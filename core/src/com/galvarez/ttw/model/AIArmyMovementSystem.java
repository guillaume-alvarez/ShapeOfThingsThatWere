package com.galvarez.ttw.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * Send the army to the tile with smallest difference between ours and second
 * influence.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class AIArmyMovementSystem extends EntityProcessingSystem {

  private static final Logger log = LoggerFactory.getLogger(AIArmyMovementSystem.class);

  private ComponentMapper<Destination> destinations;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Army> armies;

  private ComponentMapper<AIControlled> intelligences;

  private DestinationSystem destinationSystem;

  private final OverworldScreen screen;

  private final GameMap map;

  @SuppressWarnings("unchecked")
  public AIArmyMovementSystem(GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(AIControlled.class, Army.class));
    this.map = map;
    this.screen = screen;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity e) {
    Destination dest = destinations.getSafe(e);
    if (dest == null)
      return;

    AIControlled ai = intelligences.get(e);
    // no need to also check if destination is still influenced by army
    // controller: worst case is it is recreated at capital
    if (dest.target == null) {
      setNewTarget(e, ai);
    } else {
      // check we are not stuck
      MapPosition current = positions.get(e);
      if (current.equals(ai.lastPosition)) {
        // are we stuck for 3 turns?
        if (screen.getTurnNumber() - ai.lastMove > 3)
          setNewTarget(e, ai);
      } else {
        ai.lastMove = screen.getTurnNumber();
        ai.lastPosition = current;
      }
    }
  }

  private void setNewTarget(Entity army, AIControlled ai) {
    AIControlled empire = intelligences.get(armies.get(army).source);

    for (int i = 0; i < empire.armiesTargets.size(); i++) {
      MapPosition pos = empire.armiesTargets.get(i);
      if (!pos.equals(positions.get(army)) && !map.hasEntity(pos)) {
        List<MapPosition> path = destinationSystem.computePath(army, pos);
        if (path != null) {
          ai.lastMove = screen.getTurnNumber();
          ai.lastPosition = positions.get(army);
          empire.armiesTargets.remove(i);
          return;
        }
      }
    }

    log.warn("Cannot find a destination for {}", army.getComponent(Description.class));
  }

}
