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
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.components.Name;

@Wire
public final class AIDiplomaticSystem extends EntityProcessingSystem {

  private static final Logger log = LoggerFactory.getLogger(AIDiplomaticSystem.class);

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<AIControlled> ai;

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
    List<Entity> neighbors = getNeighbors(entity, diplo);
    if (diplo.knownStates.contains(State.TREATY)) {
      // sign treaty with half the neighbors
      for (int i = 0; i < neighbors.size() / 2; i++) {
        Entity target = neighbors.get(i);
        if (diplomaticSystem.getPossibleActions(diplo, target).contains(Action.SIGN_TREATY)) {
          log.info("%s wants to sign treaty with %s", entity.getComponent(Name.class), target.getComponent(Name.class));
          diplo.proposals.put(target, Action.SIGN_TREATY);
        }
      }
    }
    if (diplo.knownStates.contains(State.WAR)) {
      // declare war to half the neighbors
      for (int i = (neighbors.size() / 2) + 1; i < neighbors.size(); i++) {
        Entity target = neighbors.get(i);
        if (diplomaticSystem.getPossibleActions(diplo, target).contains(Action.DECLARE_WAR)) {
          log.info("%s wants to declare war to %s", entity.getComponent(Name.class), target.getComponent(Name.class));
          diplo.proposals.put(target, Action.DECLARE_WAR);
        }
      }
    }
  }

  /** Neighbors from the nicest to the baddest. */
  private List<Entity> getNeighbors(Entity entity, Diplomacy diplo) {
    IntIntMap neighbors = new IntIntMap(16);
    MapPosition pos = entity.getComponent(Capital.class).capital.getComponent(MapPosition.class);
    for (int i = 1; i < 10; i++) {
      // TODO really search for all tiles
      Influence inf = map.getInfluenceAt(pos.x + i, pos.y + i);
      if (inf != null && !inf.isMainInfluencer(entity) && inf.getMainInfluenceSource() != -1)
        neighbors.getAndIncrement(inf.getMainInfluenceSource(), 0, 1);
    }
    List<Entry> entries = new ArrayList<>();
    neighbors.entries().forEach(e -> entries.add(e));
    return entries.stream().sorted((e1, e2) -> Integer.compare(e1.value, e2.value)).map(e -> world.getEntity(e.key))
        .collect(toList());
  }
}
