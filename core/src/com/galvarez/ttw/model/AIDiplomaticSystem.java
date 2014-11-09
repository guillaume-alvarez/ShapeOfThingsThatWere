package com.galvarez.ttw.model;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntIntMap.Entry;
import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.components.Name;

@Wire
public final class AIDiplomaticSystem extends EntityProcessingSystem {

  private static final Logger log = LoggerFactory.getLogger(AIDiplomaticSystem.class);

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<Capital> capitals;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<AIControlled> ai;

  private ComponentMapper<InfluenceSource> sources;

  private DiplomaticSystem diplomaticSystem;

  private final GameMap map;

  @SuppressWarnings("unchecked")
  public AIDiplomaticSystem(GameMap map) {
    super(Aspect.getAspectForAll(AIControlled.class, Diplomacy.class));
    this.map = map;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity entity) {
    Diplomacy diplo = relations.get(entity);
    // TODO have a real AI algorithm for diplomacy
    // neighbors from the nicest to the baddest
    List<Entity> neighbors = getNeighboringSources(entity);
    if (diplo.knownStates.contains(State.TREATY) && neighbors.size() > 2) {
      // sign treaty with half the neighbors, not the last one (WAR for him!)
      for (int i = 0; i < neighbors.size() / 2 && i < neighbors.size() - 1; i++) {
        Entity target = empire(neighbors.get(i));
        makeProposal(entity, diplo, target, Action.SIGN_TREATY);
      }
    }
    if (diplo.knownStates.contains(State.WAR)) {
      // try to be at war with somebody, and only that somebody
      List<Entity> atWarWith = diplo.getEmpires(State.WAR);
      Entity target = neighbors.isEmpty() ? null : empire(neighbors.get(neighbors.size() - 1));
      // to change our war target, first make peace with preceding one
      for (Entity war : atWarWith) {
        if (target != war)
          makeProposal(entity, diplo, war, Action.MAKE_PEACE);
      }
      if (target != null && !atWarWith.contains(target))
        makeProposal(entity, diplo, target, Action.DECLARE_WAR);
    }
  }

  private void makeProposal(Entity entity, Diplomacy diplo, Entity target, Action action) {
    if (diplomaticSystem.getPossibleActions(diplo, target).contains(action)
    // do not change status if same proposal is already on the table
        && diplo.proposals.get(target) != action
        // do no make proposal to ourself
        && entity != target) {
      log.info("{} wants to {} with {}", entity.getComponent(Name.class), action.str, target.getComponent(Name.class));
      diplo.proposals.put(target, action);
    }
  }

  private Entity empire(Entity source) {
    InfluenceSource city = sources.get(source);
    return city.empire;
  }

  /** Neighbors from the nicest to the baddest. */
  private List<Entity> getNeighboringSources(Entity entity) {
    IntIntMap neighbors = new IntIntMap(16);
    Entity capital = capitals.get(entity).capital;
    MapPosition pos = positions.get(capital);
    for (int i = 1; i < 10; i++) {
      // TODO really search for all tiles
      addInfluencer(neighbors, capital, pos.x + i, pos.y + i);
      addInfluencer(neighbors, capital, pos.x - i, pos.y + i);
      addInfluencer(neighbors, capital, pos.x + i, pos.y - i);
      addInfluencer(neighbors, capital, pos.x - i, pos.y - i);
    }
    List<Entry> entries = new ArrayList<>();
    neighbors.entries().forEach(e -> entries.add(e));
    return entries.stream().filter(e -> e.key != capital.getId())
        .sorted((e1, e2) -> Integer.compare(e1.value, e2.value)).map(e -> world.getEntity(e.key)).collect(toList());
  }

  private void addInfluencer(IntIntMap neighbors, Entity capital, int x, int y) {
    Influence inf = map.getInfluenceAt(x, y);
    if (inf != null && !inf.isMainInfluencer(capital) && inf.hasMainInfluence())
      neighbors.getAndIncrement(inf.getMainInfluenceSource(world).getId(), 0, 1);
  }
}
