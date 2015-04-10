package com.galvarez.ttw;

import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.rendering.ColorAnimationSystem;
import com.galvarez.ttw.rendering.ScaleAnimationSystem;
import com.galvarez.ttw.rendering.SpriteAnimationSystem;
import com.galvarez.ttw.screens.CustomizeGameMenuScreen;
import com.galvarez.ttw.screens.MainMenuScreen;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

public class ThingsThatWereGame extends Game {

  public int windowWidth;

  public int windowHeight;

  private World world;

  private SpriteBatch batch;

  private CustomizeGameMenuScreen customScreen;

  private MainMenuScreen menuScreen;

  private SessionSettings settings;

  public ThingsThatWereGame(int width, int height) {
    /*
     * FIXME If height is greater that real screen height then the menu is
     * incorrectly set until the screen is resized. It appears easily on AC
     * laptop as default resolution is smaller. What happens is probably that
     * the OverworldScreen.stage is resized automatically but the resize()
     * method is not called.
     */
    windowWidth = width;
    windowHeight = height;
  }

  @Override
  public void create() {
    settings = new SessionSettings();

    startGame();
  }

  private void startGame() {
    world = new World();
    batch = new SpriteBatch();

    world.setSystem(new SpriteAnimationSystem());
    world.setSystem(new ScaleAnimationSystem());
    world.setSystem(new ExpiringSystem());
    world.setSystem(new ColorAnimationSystem());
    world.initialize();

    // this screen can modify the settings instance
    customScreen = new CustomizeGameMenuScreen(this, world, batch, settings);
    menuScreen = new MainMenuScreen(this, world, batch, customScreen);
    setScreen(menuScreen);
  }

  /** Quit current game and goes back to main menu. */
  public void returnToMainMenu() {
    if (getScreen() instanceof OverworldScreen)
      getScreen().dispose();

    // simply dispose everything and create anew!
    world.dispose();
    batch.dispose();
    customScreen.dispose();
    menuScreen.dispose();
    startGame();
  }

  /** Start a game. */
  public void startGame(boolean resetSettings) {
    setScreen(new OverworldScreen(this, batch, world,
    // copy settings because we want to keep for next game only the ones that
    // were customized in dedicated screen
        resetSettings ? new SessionSettings() : new SessionSettings(settings)));
  }

  @Override
  public void dispose() {
    super.dispose();
    world.dispose();
  }

  public void exit() {
    Gdx.app.exit();
  }

}
