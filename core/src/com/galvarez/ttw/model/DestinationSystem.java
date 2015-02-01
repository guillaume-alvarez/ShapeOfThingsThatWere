package com.galvarez.ttw.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
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
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
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

  /** Number of turns required to move to next tile. */
  private static final int TURNS_TO_MOVE = 2;

  private ComponentMapper<Destination> destinations;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Army> armies;

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
      // make sure player moves the entity at least one time
      notifications.addNotification(() -> screen.select(e, true), () -> !needDestination(e), Type.FLAG,
          "Select destination for %s...", names.get(e));
  }

  private boolean needDestination(Entity e) {
    Destination d = destinations.getSafe(e);
    if (d == null)
      return false;
    else
      return d.target == null || d.path == null || d.path.isEmpty();
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity e : entities) {
      Destination destination = destinations.get(e);
      // Destination component might have been removed during this turn
      if (destination != null && destination.path != null && !destination.path.isEmpty())
        moveToNext(e, destination);
      // else no current destination
    }
  }

  private void moveToNext(Entity e, Destination dest) {
    MapPosition current = positions.get(e);
    MapPosition next = dest.path.get(0);
    if (canMoveTo(e, next)) {
      if (++dest.progress >= TURNS_TO_MOVE) {
        dest.progress = 0;
        dest.path.remove(0);
        e.edit().remove(current).add(next);
        map.moveEntity(e, current, next);
        if (sources.has(e)) {
          Influence inf = map.getInfluenceAt(next);
          inf.addInfluenceDelta(e, inf.getMaxInfluence());
        }
        if (dest.path.isEmpty()) {
          dest.target = null;
          dest.path = null;
          if (!ai.has(e)) {
            // do not force player to move its units if he does not want to
            notifications.addNotification(() -> screen.select(e, true), null, Type.FLAG, "Finished moving %s!",
                names.get(e));
          }
          log.info("Moved {} to {}", names.get(e), next);
        }
      }
    }
  }

  public void moveTo(Entity e, MapPosition target) {
    MapPosition current = positions.get(e);
    e.edit().remove(current).add(target);
    map.moveEntity(e, current, target);
    if (destinations.has(e)) {
      Destination dest = destinations.get(e);
      dest.path = null;
      dest.target = null;
      dest.progress = 0;
    }
    if (sources.has(e)) {
      Influence inf = map.getInfluenceAt(target);
      inf.addInfluenceDelta(e, inf.getMaxInfluence());
    }
  }

  private static final Set<Terrain> CANNOT_ENTER = EnumSet
      .of(Terrain.SHALLOW_WATER, Terrain.DEEP_WATER, Terrain.ARCTIC);

  private boolean canMoveTo(Entity e, MapPosition next) {
    Terrain terrain = map.getTerrainAt(next);
    if (terrain.moveBlock() || CANNOT_ENTER.contains(terrain) || !map.getEntitiesAt(next).isEmpty())
      return false;

    if (armies.has(e))
      return map.getInfluenceAt(next).isMainInfluencer(armies.get(e).source);
    else
      return map.getInfluenceAt(next).isMainInfluencer(e);
  }

  /** Return the possible move target tiles for the source. */
  public Collection<MapPosition> getTargetTiles(Entity empire) {
    Set<MapPosition> set = new HashSet<>();
    InfluenceSource source = source(empire);
    Diplomacy treaties = relations.get(empire);
    for (MapPosition pos : source.influencedTiles) {
      for (MapPosition neighbor : map.getNeighbors(pos)) {
        if (!CANNOT_ENTER.contains(map.getTerrainAt(neighbor))) {
          Influence inf = map.getInfluenceAt(neighbor);
          if (!inf.hasMainInfluence())
            set.add(neighbor);
          else if (treaties.getRelationWith(inf.getMainInfluenceSource(world)) != State.TREATY)
            set.add(neighbor);
        }
      }
    }
    return set;
  }

  private InfluenceSource source(Entity e) {
    if (sources.has(e))
      return sources.get(e);
    else
      return sources.get(armies.get(e).source);
  }

  public void computePath(Entity e, MapPosition target) {
    Destination dest = destinations.get(e);
    dest.target = target;
    dest.path = astar.aStarSearch(positions.get(e), target, p -> !CANNOT_ENTER.contains(map.getTerrainAt(p)));
    dest.progress = 0;
  }

}
