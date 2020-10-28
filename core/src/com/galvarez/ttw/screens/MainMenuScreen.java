package com.galvarez.ttw.screens;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * This screen presents the user with a menu to launch the game.
 *
 * @author Guillaume Alvarez
 */
public final class MainMenuScreen extends AbstractScreen {

  private final Stage stage;

  private final FramedMenu title;

  private final FramedMenu menu;

  private final AbstractScreen customGame;

  public MainMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, AbstractScreen customGame) {
    super(game, world, batch);
    this.customGame = customGame;

    stage = new Stage(new ScreenViewport(), batch);

    Skin skin = game.assets.getSkin();
    title = new FramedMenu(skin, 800, 600).nbColumns(1);
    menu = new FramedMenu(skin, 800, 600).nbColumns(1);

    initMenu();
  }

  private void initMenu() {
    title.clear();
    LabelStyle style = new LabelStyle(game.assets.getSkin().get(LabelStyle.class));
    style.font = game.assets.getFont(32);
    Label l = new Label("The Shape of Things that Were", style);
    l.setAlignment(Align.center);
    title.getTable().add(l).left().colspan(1).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());
    title.getTable().row();

    title.addLabel("Growth of ancient civilizations").setAlignment(Align.center);
    title.addToStage(stage, 30, stage.getHeight() - 30, false);

    menu.clear();
    menu.addButton("Start new game (default parameters)", () -> game.startGame(true));
    menu.addButton("Start new game (custom parameters)", () -> game.setScreen(customGame));
    menu.addToStage(stage, 30, title.getY() - 30, false);
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
