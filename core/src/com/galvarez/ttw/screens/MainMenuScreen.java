package com.galvarez.ttw.screens;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * This screen presents the user with a menu to launch the game.
 * 
 * @author Guillaume Alvarez
 */
public final class MainMenuScreen extends AbstractScreen {

  private final Stage stage;

  private final FramedMenu menu;

  private final AbstractScreen customGame;

  public MainMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, AbstractScreen customGame) {
    super(game, world, batch);
    this.customGame = customGame;

    stage = new Stage(new ScreenViewport(), batch);

    FileHandle skinFile = new FileHandle("resources/uiskin/uiskin.json");
    Skin skin = new Skin(skinFile);
    menu = new FramedMenu(skin, 800, 600);

    initMenu();
  }

  private void initMenu() {
    menu.clear();
    menu.addButton("Start new game (default parameters)", () -> game.startGame(new SessionSettings()));
    menu.addButton("Start new game (custom parameters)", () -> game.setScreen(customGame));
    menu.addToStage(stage, 30, stage.getHeight() - 30, false);
  }

  @Override
  public void show() {
    super.show();

    Gdx.input.setInputProcessor(stage);
  }

  @Override
  public void render(float delta) {
    super.render(delta);

    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);

    stage.getViewport().update(width, height, true);
    /*
     * The GUI use a ScreenViewport, meaning it won't scale when screen size
     * change. This is fine because we don't want the GUI size to change,
     * becoming zoomed in and ugly or zoomed and unreadable. However it has a
     * small side effect: the existing menu were placed according to the
     * vertical stage size. The stage size changed with the screen (game window)
     * one. So we must recompute the GUI elements coordinates. The simlest way
     * to do it is to recreate the menu.
     */
    initMenu();
  }

  @Override
  public void dispose() {
    super.dispose();

    stage.dispose();
  }

}
