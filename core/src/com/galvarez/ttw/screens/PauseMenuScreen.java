package com.galvarez.ttw.screens;

import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * This screen appears when user tries to pause or escape from the main game
 * screen.
 * 
 * @author Guillaume Alvarez
 */
public final class PauseMenuScreen extends AbstractPausedScreen<AbstractScreen> {

  private final FramedMenu menu;

  public PauseMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, AbstractScreen gameScreen) {
    super(game, world, batch, gameScreen);

    menu = new FramedMenu(skin, 800, 600);

    initMenu();
  }

  @Override
  protected void initMenu() {
    menu.clear();
    menu.addButton("Resume game", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        resumeGame();
      }
    }, true);
    menu.addButton("Return to main menu", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        game.returnToMainMenu();
      }
    }, true);
    menu.addButton("Exit game", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        game.exit();
      }
    }, true);
    menu.addToStage(stage, 30, stage.getHeight() - 30, false);
  }
}
