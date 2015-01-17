package com.galvarez.ttw.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.model.map.PathFinding;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.NotificationsSystem.Type;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * Moves the entities having a {@link Destination} across the map.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class DestinationSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(DestinationSystem.class);

  private ComponentMapper<Destination> destinations;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Name> names;

  private ComponentMapper<AIControlled> ai;

  private NotificationsSystem notifications;

  private final GameMap map;

  private final PathFinding astar;

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public DestinationSystem(GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(Destination.class, MapPosition.class));
    this.map = map;
    this.screen = screen;
    this.astar = new PathFinding(map);
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    if (!ai.has(e))
      notifications.addNotification(() -> screen.select(e), () -> needDestination(e), Type.FLAG,
          "Select destination for %s...", names.get(e));
  }

  private boolean needDestination(Entity e) {
    return !destinations.has(e) || destinations.get(e).target != null;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity e : entities) {
      Destination destination = destinations.get(e);
      if (destination.path != null)
        moveToNext(e, destination);
      // else no current destination
    }
  }

  private void moveToNext(Entity e, Destination dest) {
    MapPosition current = positions.get(e);
    MapPosition next = dest.path.get(0);
    if (canMoveTo(e, next)) {
      dest.path.remove(0);
      e.edit().remove(current).add(next);
      map.setEntity(null, current);
      map.setEntity(e, next);
      if (sources.has(e)) {
        Influence inf = map.getInfluenceAt(next);
        inf.addInfluenceDelta(e, inf.getMaxInfluence());
      }
      if (dest.path.isEmpty()) {
        dest.target = null;
        dest.path = null;
        if (!ai.has(e))
          notifications.addNotification(() -> screen.select(e), () -> needDestination(e), Type.FLAG,
              "Finished moving %s!", names.get(e));
        log.info("Moved {} to {}", names.get(e), next);
      }
    }
  }

  private static final Set<Terrain> CANNOT_ENTER = EnumSet
      .of(Terrain.SHALLOW_WATER, Terrain.DEEP_WATER, Terrain.ARCTIC);

  public boolean canMoveTo(Entity e, MapPosition next) {
    Terrain terrain = map.getTerrainAt(next);
    if (terrain.moveBlock() || CANNOT_ENTER.contains(terrain) || map.getEntityAt(next) != null)
      return false;

    Influence inf = map.getInfluenceAt(next);
    return inf.isMainInfluencer(e) || !inf.hasMainInfluence();
  }

  /** Return the possible move target tiles for the source. */
  public Collection<MapPosition> getTargetTiles(Entity e) {
    Set<MapPosition> set = new HashSet<>();
    InfluenceSource source = sources.get(e);
    Diplomacy treaties = relations.get(source.empire);
    for (MapPosition pos : source.influencedTiles) {
      for (MapPosition neighbor : MapTools.getNeighbors(pos)) {
        if (canMoveTo(e, neighbor)) {
          Influence inf = map.getInfluenceAt(neighbor);
          if (!inf.hasMainInfluence())
            set.add(neighbor);
          else {
            InfluenceSource neighborSource = sources.get(inf.getMainInfluenceSource(world));
            if (treaties.getRelationWith(neighborSource.empire) != State.TREATY)
              set.add(neighbor);
          }
        }
      }
    }
    return set;
  }

  public List<MapPosition> computePath(Entity e, Destination dest) {
    return astar.aStarSearch(positions.get(e), dest.target, p -> canMoveTo(e, p));
  }

}
