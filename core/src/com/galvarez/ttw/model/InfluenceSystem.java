package com.galvarez.ttw.model;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntIntMap.Entry;
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

  private ComponentMapper<Empire> data;

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

    // prepare new influence target that will be computed
    for (int x = 0; x < map.width; x++)
      for (int y = 0; y < map.height; y++)
        map.getInfluenceAt(x, y).clearInfluenceTarget();

    // and update source power
    for (Entity empire : empires) {
      updateInfluencedTiles(empire);
      accumulatePower(empire);
      checkInfluencedByOther(sources.get(empire), empire);
    }

    // then compute the new delta for every entity and tile
    for (Entity empire : empires) {
      IntIntMap armyInfluenceOn = new IntIntMap();
      Diplomacy diplo = relations.get(empire);
      int armyPower = commands.get(empire).militaryPower;
      for (Entity enemy : diplo.getEmpires(State.WAR))
        armyInfluenceOn.put(enemy.getId(), armyPower - commands.get(enemy).militaryPower);

      updateInfluenceTarget(sources.get(empire), empire, armyInfluenceOn);
    }

    // finally compute new influence at start of turn
    for (int x = 0; x < map.width; x++)
      for (int y = 0; y < map.height; y++)
        updateTileInfluence(x, y);
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

          // keep influence on own tile...
          tile.moveInfluence(influencer, empire);
          source.influencedTiles.add(tile.position);
          // ...and neighbors
          for (Border b : Border.values()) {
            Influence t = map.getInfluenceAt(b.getNeighbor(tile.position));
            // do not modify influence if there is an army or city on the tile
            if (t != null && !map.hasEntity(t.position)) {
              t.moveInfluence(influencer, empire);
              if (t.isMainInfluencer(empire))
                source.influencedTiles.add(t.position);
            }
          }
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

  private static final String[] DELTA_STR = { "----", "---", "--", "-", "", "+", "++", "+++", "++++" };

  private void updateTileInfluence(int x, int y) {
    Influence tile = map.getInfluenceAt(x, y);
    float shift = 0f;
    for (IntIntMap.Entry e : tile.getDelta()) {
      if (e.value != 0) {
        Entity empire = world.getEntity(e.key);
        String text = abs(e.value) > 4 ? (e.value > 0 ? "+" + e.value : Integer.toString(e.value))
            : DELTA_STR[e.value + 4];
        EntityFactory.createFadingTileLabel(world, text, data.get(empire).color, x, y + shift, 1f);
        shift += 0.2;
      }
    }
    tile.computeNewInfluence();
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
  private void updateInfluenceTarget(InfluenceSource source, Entity e, IntIntMap armyInfluenceOn) {
    // first compute the target influence from the city itself
    for (ObjectIntMap.Entry<Influence> entry : getTargetInfluence(e, positions.get(e), source.power())) {
      Influence tile = entry.key;
      int target = entry.value;

      // start losing influence when no neighboring tile
      if (canInfluence(e, tile))
        tile.increaseTarget(e, target);
      else {
        // pass influence to protectorates
        for (Entry inf : tile) {
          Entity influencer = world.getEntity(inf.key);
          if (relations.get(influencer).getRelationWith(e) == State.PROTECTORATE)
            tile.increaseTarget(influencer, target);
        }
      }

      // do not forget the military bonus from war
      if (tile.hasMainInfluence())
        tile.increaseTarget(e, armyInfluenceOn.get(tile.getMainInfluenceSource(), 0));
    }

    // then add influence from its armies
    for (Entity s : source.secondarySources) {
      for (ObjectIntMap.Entry<Influence> entry : getTargetInfluence(e, positions.get(s), armies.get(s).currentPower)) {
        // armies only add to influence, they do not reduce it
        Influence inf = entry.key;
        int target = entry.value;

        if (target > 0 && canInfluence(e, inf))
          inf.increaseTarget(e, target);
      }
    }
  }

  /**
   * Compute the target influence on all tiles around the starting position.
   * <p>
   * Note: resulting target can be negative. It stops on tiles where there is no
   * influence from source AND target is not positive.
   */
  private ObjectIntMap<Influence> getTargetInfluence(Entity source, MapPosition startPos, int startingPower) {
    Map<Terrain, Integer> costs = terrainCosts(source);
    Queue<Pos> frontier = new PriorityQueue<>();
    frontier.add(new Pos(startPos, startingPower));
    ObjectIntMap<Influence> targets = new ObjectIntMap<>();
    targets.put(map.getInfluenceAt(startPos), startingPower);

    while (!frontier.isEmpty()) {
      Pos current = frontier.poll();

      for (MapPosition next : map.getNeighbors(current.pos)) {
        Influence inf = map.getInfluenceAt(next);
        if (!inf.terrain.moveBlock()) {
          int newTarget = current.target - costs.get(inf.terrain);
          int oldTarget = targets.get(inf, Integer.MIN_VALUE);
          if (newTarget > oldTarget) {
            targets.put(inf, newTarget);
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
  private boolean canInfluence(Entity source, Influence inf) {
    if (inf.isMainInfluencer(source))
      return true;

    // cannot influence on tiles from empires we have a treaty with
    if (inf.hasMainInfluence()) {
      Diplomacy treaties = relations.get(source);
      State relation = treaties.getRelationWith(inf.getMainInfluenceSource(world));
      if (relation == State.TREATY || relation == State.PROTECTORATE)
        return false;
    }

    // need a neighbor we already have influence on
    for (Border b : Border.values()) {
      Influence tile = map.getInfluenceAt(b.getNeighbor(inf.position));
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

    source.influencedTiles.removeIf(p -> !map.getInfluenceAt(p).isMainInfluencer(e));
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
