package com.galvarez.ttw.screens.overworld.controls;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

// TODO remove this useless class and let screen manage its logic?
public class MenuProcessor {

  private final OverworldScreen screen;

  private final InputManager inputManager;

  Stage stage;

  public MenuProcessor(OverworldScreen screen, InputManager inputManager, Stage stage) {
    this.screen = screen;
    this.inputManager = inputManager;
    this.stage = stage;
  }

  public void endTurn() {
    screen.processTurn();

    // update the menu data
    inputManager.reloadMenus();
  }

  public void pauseMenu() {
    screen.pauseMenu();
  }

  public void diplomacyMenu() {
    screen.diplomacyMenu();
  }

  public void discoveryMenu() {
    screen.discoveryMenu();
  }

}
