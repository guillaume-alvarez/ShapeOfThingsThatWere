package com.galvarez.ttw.model;

import java.util.Comparator;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * Create new armies when available military power is sufficient. Avoid creating
 * many little armies: create a new army if available power is not negligible
 * with respect to used power.
 * <p>
 * Also compute preferred destinations for armies, to be used when an army is
 * moving.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class AIArmyCommandSystem extends EntityProcessingSystem {

  private ComponentMapper<ArmyCommand> commands;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<AIControlled> intelligences;

  private ArmiesSystem armiesSystem;

  private final GameMap map;

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public AIArmyCommandSystem(GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(AIControlled.class, ArmyCommand.class, InfluenceSource.class));
    this.map = map;
    this.screen = screen;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity e) {
    ArmyCommand command = commands.get(e);
    int available = command.militaryPower - command.usedPower;
    if (available > 0 && available > command.usedPower / 2) {
      armiesSystem.createNewArmy(e, available);
    }

    // do not compute destinations at every turn
    AIControlled ai = intelligences.get(e);
    InfluenceSource source = sources.get(e);
    if (ai.armiesTargets.size() < source.secondarySources.size()
        || ai.armiesTargetsComputationTurn + 10 < screen.getTurnNumber()) {
      ai.armiesTargets.clear();

      ai.armiesTargets.addAll(source.influencedTiles);
      // send the armies to the influenced tiles with the smallest difference
      // between ours and second influence
      ai.armiesTargets.sort(Comparator.comparingInt(p -> map.getInfluenceAt(p).getSecondInfluenceDiff()));
    }

  }

}
