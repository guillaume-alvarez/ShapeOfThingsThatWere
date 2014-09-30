package com.galvarez.ttw.model;

import static java.lang.Math.max;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.model.map.MapTools.Border;
import com.galvarez.ttw.rendering.components.Name;

@Wire
public final class InfluenceSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(InfluenceSystem.class);

  public static final int INITIAL_POWER = 100;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<MapPosition> positions;

  private final GameMap map;

  @SuppressWarnings("unchecked")
  public InfluenceSystem(GameMap gameMap) {
    super(Aspect.getAspectForAll(InfluenceSource.class));
    this.map = gameMap;
  }

  @Override
  protected void inserted(Entity e) {
    InfluenceSource source = sources.get(e);
    source.power = INITIAL_POWER;
    if (source.power > 0) {
      MapPosition pos = positions.get(e);

      // first influence own tile
      Influence tile = map.getInfluenceAt(pos);
      tile.setInfluence(e, tile.terrain.moveCost());
      source.influencedTiles.add(pos);
    }
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    // must apply each step to all sources to have a consistent behavior

    // first apply delta from previous turn and display it
    for (int x = 0; x < map.height; x++) {
      for (int y = 0; y < map.width; y++) {
        updateTileInfluence(x, y);
      }
    }

    // and update source power
    for (Entity e : entities) {
      updateInfluencedTiles(e);
      accumulatePower(e);
    }

    // then compute the delta for every entity and tile
    for (Entity e : entities) {
      InfluenceSource source = sources.get(e);

      if (!checkInfluencedByOther(source, e)) {
        addDistanceDelta(source, e);
        addFlagDelta(source, e);
      }
    }
  }

  /**
   * When a city tile main influence belongs to an other empire, we switch the
   * empire city: it was conquered.
   */
  private boolean checkInfluencedByOther(InfluenceSource source, Entity e) {
    Influence tile = map.getInfluenceAt(positions.get(e));
    if (tile.isMainInfluencer(e))
      return false;

    InfluenceSource influencer = sources.get(world.getEntity(tile.getMainInfluenceSource()));
    if (source.empire == influencer.empire)
      return false;

    log.info("{} conquered by {}, will no longer influence tiles for {}",
        e.getComponent(Name.class).name, influencer.empire, source.empire);
    if (source.empire.culture == influencer.empire.culture)
      source.power = max(1, source.power / 2);
    else
      source.power = 1;
    source.empire = influencer.empire;
    return true;
  }

  private void updateTileInfluence(int x, int y) {
    Influence tile = map.getInfluenceAt(x, y);
    for (IntIntMap.Entry e : tile.getDelta()) {
      if (e.value != 0) {
        InfluenceSource source = sources.get(world.getEntity(e.key));
        EntityFactory.createInfluenceLabel(world, e.value > 0 ? "+" + e.value : Integer.toString(e.value),
            source.empire.color, x, y).addToWorld();
      }
    }
    tile.applyDelta();
    // do not forget to update the main source
    int main = tile.getMainInfluenceSource();
    if (main != -1)
      sources.get(world.getEntity(main)).influencedTiles.add(map.getPositionAt(x, y));
  }

  /**
   * Spread source influence around the source. For each tile already influenced
   * or next to one we compute the minimal distance to the source. Then from the
   * distance we compute the target influence level. Then apply the delta.
   */
  private void addDistanceDelta(InfluenceSource source, Entity e) {
    Map<MapPosition, Integer> distances = getDistances(e, positions.get(e));

    for (Entry<MapPosition, Integer> entry : distances.entrySet()) {
      Influence tile = map.getInfluenceAt(entry.getKey());
      int target = canInfluence(e, entry.getKey()) ? target = max(0, source.power - max(0, entry.getValue().intValue()))
          // start losing influence when no neighboring tile
          : 0;
      int current = tile.getInfluence(e);
      if (target > current)
        tile.addInfluenceDelta(e, max(1, (target - current) / 10));
      else if (target < current)
        tile.addInfluenceDelta(e, -max(1, (current - target) / 10));
      // do nothing if same obviously
    }
  }

  private void addFlagDelta(InfluenceSource source, Entity e) {
    if (source.target != null && canInfluence(e, source.target))
      map.getInfluenceAt(source.target).addInfluenceDelta(e, 10);
    else
      // must remove flag: target is not correct
      source.target = null;
  }

  /**
   * Remove from {@link InfluenceSource#influencedTiles} all the tiles the
   * entity is no longer the main influencer on.
   */
  private void updateInfluencedTiles(Entity e) {
    InfluenceSource source = sources.get(e);

    Predicate<MapPosition> isNotMain = p -> !map.getInfluenceAt(p).isMainInfluencer(e);
    source.influencedTiles.removeIf(isNotMain);
  }

  /**
   * Update the source power. Each turn we add the number of influenced tiles to
   * the advancement. When it reaches 10*(power+1) then power is increased by 1
   * and advancement is reset. The new power is also added to the influence on
   * source tile.
   * <p>
   * Power is lost when the current influenced size is 10 times below the
   * current power.
   */
  private void accumulatePower(Entity e) {
    InfluenceSource source = sources.get(e);

    source.powerAdvancement += source.influencedTiles.size() - source.power / 10;
    if (source.powerAdvancement < 0) {
      source.power--;
      source.powerAdvancement = getRequiredPowerAdvancement(source);
    } else if (source.powerAdvancement >= getRequiredPowerAdvancement(source)) {
      source.powerAdvancement = 0;
      source.power++;
    }
  }

  public int getRequiredPowerAdvancement(InfluenceSource source) {
    // TODO the base power should depend on the empire
    return 10 * (source.power + 1);
  }

  /**
   * Can only influence a tile if it belongs to the source or one of its
   * neighbor does.
   */
  public boolean canInfluence(Entity source, MapPosition pos) {
    // that tile should never be null: there is a flag on it
    if (map.getInfluenceAt(pos).isMainInfluencer(source))
      return true;

    for (Border b : Border.values()) {
      MapPosition neighbor = MapTools.getNeighbor(b, pos.x, pos.y);
      Influence tile = map.getInfluenceAt(neighbor);
      if (tile != null && tile.isMainInfluencer(source))
        return true;
    }
    return false;
  }

  /** Return the flaggable tiles for the source. */
  public Array<MapPosition> getFlaggableTiles(Entity e) {
    Set<MapPosition> set = new HashSet<>();
    InfluenceSource source = sources.get(e);
    for (MapPosition pos : source.influencedTiles) {
      for (MapPosition neighbor : MapTools.getNeighbors(pos))
        set.add(neighbor);
    }
    return new Array<>(set.toArray(new MapPosition[0]));
  }

  public Map<MapPosition, Integer> getDistances(Entity source, MapPosition pos) {
    Map<MapPosition, Integer> distances = new HashMap<>();
    distances.put(pos, 0);
    collectDistances(source, 0, pos, distances);
    return distances;
  }

  private void collectDistances(Entity source, int distance, MapPosition pos, Map<MapPosition, Integer> distances) {
    for (MapPosition neighbor : MapTools.getNeighbors(pos)) {
      Influence inf = map.getInfluenceAt(neighbor);
      if (!inf.terrain.moveBlock()) {
        Integer old = distances.get(neighbor);
        int newDistance = distance + inf.terrain.moveCost();
        if (old == null || newDistance < old.intValue()) {
          distances.put(neighbor, Integer.valueOf(newDistance));
          if (inf.hasInfluence(source))
            // only one tile from already influenced tiles
            collectDistances(source, newDistance, neighbor, distances);
        }
      }
    }
  }

}
