package com.galvarez.ttw.screens;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.ArmiesSystem;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * This screen appears when user selects the armies menu. It lists the existing
 * player armies and permits to create new ones.
 * 
 * @author Guillaume Alvarez
 */
public final class ArmiesMenuScreen extends AbstractPausedScreen<AbstractScreen> {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ArmiesMenuScreen.class);

  private final FramedMenu topMenu;

  private final FramedMenu listMenu;

  private final FramedMenu armyMenu;

  private final Entity player;

  private final ArmiesSystem system;

  public ArmiesMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, AbstractScreen gameScreen,
      Entity player, ArmiesSystem system) {
    super(game, world, batch, gameScreen);
    this.player = player;
    this.system = system;

    topMenu = new FramedMenu(skin, 800, 600);
    listMenu = new FramedMenu(skin, 800, 600);
    armyMenu = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    createTopMenu();

    createListMenu();

    createArmyMenu(null);
  }

  private void createTopMenu() {
    topMenu.clear();
    topMenu.addButton("Resume game", this::resumeGame);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);
  }

  private void createListMenu() {
    listMenu.clear();

    List<Entity> armies = system.getArmies(player);

    if (armies.isEmpty())
      listMenu.addLabel("- No active army -");
    else {
      listMenu.addLabel("- " + armies.size() + " active arm" + (armies.size() > 1 ? "ies" : "y") + " -");
      for (Entity e : armies) {
        Army army = e.getComponent(Army.class);
        listMenu.addButton(e.getComponent(Name.class).name + " - power=" + army.currentPower + "/" + army.maxPower,
            () -> createArmyMenu(e));
      }
    }

    ArmyCommand command = player.getComponent(ArmyCommand.class);
    int availablePower = command.militaryPower - command.usedPower;
    if (availablePower > 0)
      listMenu.addButton("Create new army with " + availablePower + " power (of " + command.militaryPower + ")",
          () -> {
            Entity newArmy = system.createNewArmy(player, availablePower);
            if (newArmy != null) {
              createListMenu();
              createArmyMenu(newArmy);
            }
          });
    else
      listMenu.addLabel("- Cannot create a new army: no available military power. -");

    listMenu.addToStage(stage, 30, topMenu.getY() - 30, false);
  }

  private void createArmyMenu(Entity selected) {
    armyMenu.clear();

    if (selected == null) {
      armyMenu.addLabel("- No selected army -");
    } else {
      armyMenu.addLabel("- Selected '" + selected.getComponent(Description.class) + "' -");
      Army army = selected.getComponent(Army.class);
      armyMenu.addLabel("  Power = " + army.currentPower + "/" + army.maxPower);
    }

    armyMenu.addToStage(stage, 30, listMenu.getY() - 30, false);
  }

}
