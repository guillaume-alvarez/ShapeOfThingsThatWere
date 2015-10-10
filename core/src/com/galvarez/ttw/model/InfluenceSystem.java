package com.galvarez.ttw.model;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.InfluenceSource.Modifiers;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools.Border;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.components.Description;

/**
 * This classes computes the influence from the different sources (i.e. cities)
 * and update it every turn.
 * <p>
 * The game is centered on the influence idea. It starts from a source with a
 * certain power and flow on the neighboring map tiles. The throughput depends
 * on the source power, distance to the source and terrain cost/difficulty. When
 * multiple sources influence the same tile, it belongs to the source with the
 * highest influence score. For ease of understanding, influence is expressed as
 * percentages.
 * <p>
 * Ideally the influence progression should be:
 * <ul>
 * <li>at a constant pace and continuous from a turn to the next (for instance
 * one tile per turn)
 * <li>with decreasing values from the civilized center to the wild or disputed
 * border
 * <li>on a small scale so that close influence sources can fight for tiles
 * <p>
 * The model that works the best seems to be a linear interpolation of the
 * target influence. The target influence is computed as a waterfall algorithm
 * from the source.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class InfluenceSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(InfluenceSystem.class);

  public static final int INITIAL_POWER = 100;

  private ComponentMapper<Empire> empires;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<ArmyCommand> commands;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Army> armies;

  private DiplomaticSystem diplomaticSystem;

  private final GameMap map;

  @SuppressWarnings("unchecked")
  public InfluenceSystem(GameMap gameMap) {
    super(Aspect.getAspectForAll(InfluenceSource.class));
    this.map = gameMap;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    InfluenceSource source = sources.get(e);
    if (source.power() > 0) {
      MapPosition pos = positions.get(e);

      // first influence own tile
      Influence tile = map.getInfluenceAt(pos);
      if (tile.hasMainInfluence()) {
        Entity main = tile.getMainInfluenceSource(world);
        tile.setInfluence(e, tile.getMaxInfluence() + tile.getDelta(main) + 1);
      } else {
        tile.setInfluence(e, tile.getMaxInfluence());
      }
      source.influencedTiles.add(pos);
    }
  }

  @Override
  protected void removed(Entity e) {
    super.removed(e);
    for (int x = 0; x < map.width; x++)
      for (int y = 0; y < map.height; y++)
        map.getInfluenceAt(x, y).removeInfluence(e);
    map.setEntity(null, positions.get(e));

    InfluenceSource source = sources.get(e);
    source.influencedTiles.clear();
    for (Entity army : source.secondarySources)
      map.setEntity(null, positions.get(army));
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> empires) {
    // must apply each step to all sources to have a consistent behavior

    // first apply delta from previous turn and display it
    for (int x = 0; x < map.height; x++) {
      for (int y = 0; y < map.width; y++) {
        updateTileInfluence(x, y);
      }
    }

    // and update source power
    for (Entity city : empires) {
      updateInfluencedTiles(city);
      accumulatePower(city);
    }

    // then compute the delta for every entity and tile
    for (Entity empire : empires) {
      IntIntMap armyInfluenceOn = new IntIntMap();
      Diplomacy diplo = relations.get(empire);
      int armyPower = commands.get(empire).militaryPower;
      for (Entity enemy : diplo.getEmpires(State.WAR))
        armyInfluenceOn.put(enemy.getId(), armyPower - commands.get(enemy).militaryPower);

      InfluenceSource source = sources.get(empire);
      checkInfluencedByOther(source, empire);
      addInfluenceDelta(source, empire, armyInfluenceOn);
    }
  }

  /**
   * When a city tile main influence belongs to an other empire, we add a
   * tribute diplomatic relation.
   */
  private void checkInfluencedByOther(InfluenceSource source, Entity empire) {
    Influence tile = map.getInfluenceAt(positions.get(empire));
    if (!tile.isMainInfluencer(empire) && tile.hasMainInfluence()) {
      Diplomacy loser = relations.get(empire);
      Entity influencer = tile.getMainInfluenceSource(world);
      if (empire != influencer && loser.getRelationWith(influencer) != State.TRIBUTE) {
        log.info("{} conquered by {}, will now be tributary to its conqueror.", empire.getComponent(Description.class),
            influencer.getComponent(Description.class));
        source.addToPower(-1);
        sources.get(influencer).addToPower(1);
        if (source.power() <= 0) {
          log.info("{} conquered by {}, was destroyed.", empire.getComponent(Description.class),
              influencer.getComponent(Description.class));
          delete(empire);
        } else {
          diplomaticSystem.clearRelations(empire, loser);
          diplomaticSystem.changeRelation(empire, loser, influencer, relations.get(influencer), Action.SURRENDER);
          loser.proposals.remove(influencer);

          // keep influence on own tile
          tile.setInfluence(empire, tile.getMaxInfluence() + 1);
          source.influencedTiles.add(tile.position);
        }
      }
    }
  }

  private void delete(Entity entity) {
    log.info("{} ({}) is deleted", entity.getComponent(Description.class), entity);
    map.setEntity(null, positions.get(entity));
    if (sources.has(entity))
      for (Entity s : sources.get(entity).secondarySources)
        delete(s);
    entity.edit().deleteEntity();
  }

  private void updateTileInfluence(int x, int y) {
    Influence tile = map.getInfluenceAt(x, y);
    for (IntIntMap.Entry e : tile.getDelta()) {
      if (e.value != 0) {
        Entity empire = world.getEntity(e.key);
        EntityFactory.createFadingTileLabel(world, e.value > 0 ? "+" + e.value : Integer.toString(e.value),
            empires.get(empire).color, x, y, 1f);
      }
    }
    tile.applyDelta();
    // do not forget to update the main source
    Entity main = tile.getMainInfluenceSource(world);
    if (main != null)
      sources.get(main).influencedTiles.add(map.getPositionAt(x, y));
  }

  /**
   * Spread source influence around the source. For each tile already influenced
   * or next to one we compute the minimal distance to the source. Then from the
   * distance we compute the target influence level. Then apply the delta.
   */
  private void addInfluenceDelta(InfluenceSource source, Entity e, IntIntMap armyInfluenceOn) {
    ObjectIntMap<MapPosition> targets = new ObjectIntMap<>();
    for (ObjectIntMap.Entry<MapPosition> entry : getTargetInfluence(e, positions.get(e), source.power())) {
      Influence tile = map.getInfluenceAt(entry.key);
      int target = canInfluence(e, entry.key) ? entry.value
      // start losing influence when no neighboring tile
          : 0;
      // do not forget the military from war
      if (tile.hasMainInfluence())
        target += armyInfluenceOn.get(tile.getMainInfluenceSource(), 0);
      targets.put(entry.key, target);
    }

    for (Entity s : source.secondarySources) {
      for (ObjectIntMap.Entry<MapPosition> entry : getTargetInfluence(e, positions.get(s), armies.get(s).currentPower)) {
        MapPosition pos = entry.key;
        // armies only add to influence, they do not reduce it
        if (entry.value > 0 && canInfluence(e, pos))
          targets.getAndIncrement(pos, 0, entry.value);
      }
    }

    for (ObjectIntMap.Entry<MapPosition> entry : targets) {
      Influence tile = map.getInfluenceAt(entry.key);
      int current = tile.getInfluence(e);
      int target = entry.value;
      if (target > current)
        tile.addInfluenceDelta(e, max(1, (target - current) / 10));
      else if (target < current)
        tile.addInfluenceDelta(e, -max(1, (current - target) / 10));
      // do nothing if same obviously
    }
  }

  /**
   * Compute the target influence on all tiles around the starting position.
   * <p>
   * Note: resulting target can be negative. It stops on tiles where there is no
   * source influence AND target is not positive.
   */
  private ObjectIntMap<MapPosition> getTargetInfluence(Entity source, MapPosition startPos, int startingPower) {
    Map<Terrain, Integer> costs = terrainCosts(source);
    Queue<Pos> frontier = new PriorityQueue<>();
    frontier.add(new Pos(startPos, startingPower));
    ObjectIntMap<MapPosition> targets = new ObjectIntMap<MapPosition>();
    targets.put(startPos, startingPower);

    while (!frontier.isEmpty()) {
      Pos current = frontier.poll();

      for (MapPosition next : map.getNeighbors(current.pos)) {
        Influence inf = map.getInfluenceAt(next);
        if (!inf.terrain.moveBlock()) {
          int newTarget = current.target - costs.get(inf.terrain);
          int oldTarget = targets.get(next, Integer.MIN_VALUE);
          if (newTarget > oldTarget) {
            targets.put(next, newTarget);
            /*
             * Increase only one tile from already influenced tiles. Decreases
             * wherever we have some influence (makes no sense to decrease
             * elsewhere.
             */
            if (inf.hasInfluence(source))
              frontier.offer(new Pos(next, newTarget));
          }
        }
      }
    }
    return targets;
  }

  private static final class Pos implements Comparable<Pos> {
    private final MapPosition pos;

    private final int target;

    private Pos(MapPosition pos, int target) {
      this.pos = pos;
      this.target = target;
    }

    @Override
    public int compareTo(Pos o) {
      return -Integer.compare(target, o.target);
    }
  }

  private Map<Terrain, Integer> terrainCosts(Entity source) {
    Modifiers modifiers = sources.get(source).modifiers;
    Map<Terrain, Integer> res = new EnumMap<>(Terrain.class);
    for (Terrain t : Terrain.values())
      res.put(t, max(1, t.moveCost() - modifiers.terrainBonus.get(t)));
    return res;
  }

  /**
   * Can only influence a tile if it belongs to the source or one of its
   * neighbor does. Cannot influence if we have a treaty.
   */
  private boolean canInfluence(Entity source, MapPosition pos) {
    Influence influence = map.getInfluenceAt(pos);
    if (influence.isMainInfluencer(source))
      return true;

    // cannot influence on tiles from empires we have a treaty with
    if (influence.hasMainInfluence()) {
      Diplomacy treaties = relations.get(source);
      if (treaties.getRelationWith(influence.getMainInfluenceSource(world)) == State.TREATY)
        return false;
    }

    // need a neighbor we already have influence on
    for (Border b : Border.values()) {
      Influence tile = map.getInfluenceAt(b.getNeighbor(pos));
      if (tile != null && tile.isMainInfluencer(source))
        return true;
    }
    return false;
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
   * the advancement. When it reaches the threshold then power is increased by 1
   * and advancement is reset.
   * <p>
   * Power is lost when cities revolt.
   */
  private void accumulatePower(Entity empire) {
    InfluenceSource source = sources.get(empire);

    int increase = source.growth * source.power();
    if (increase > 0) {
      Diplomacy diplomacy = relations.get(empire);
      List<Entity> tributes = diplomacy.getEmpires(State.TRIBUTE);
      int remains = increase;
      for (Entity other : tributes) {
        int tribute = min(remains, increase / tributes.size());
        sources.get(other).addToPower(tribute / 1000f);
        remains -= tribute;
      }
      increase = remains;
    }

    source.addToPower(increase / 1000f);
  }

}
