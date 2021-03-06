package com.galvarez.ttw.screens;

import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * This screen appears when user tries to pause or escape from the main game
 * screen.
 * 
 * @author Guillaume Alvarez
 */
public final class PauseMenuScreen extends AbstractPausedScreen<AbstractScreen> {

  private final FramedMenu menu;

  public PauseMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen) {
    super(game, world, batch, gameScreen);

    menu = new FramedMenu(skin, 800, 600);

    initMenu();
  }

  @Override
  protected void initMenu() {
    menu.clear();
    menu.addButton("Resume game", this::resumeGame);
    menu.addButton("Return to main menu", game::returnToMainMenu);
    menu.addButton("Exit game", game::exit);
    menu.addToStage(stage, 30, stage.getHeight() - 30, false);
  }
}
