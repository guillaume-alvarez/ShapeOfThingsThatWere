package com.galvarez.ttw.screens;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.galvarez.ttw.ThingsThatWereGame;

public abstract class AbstractPausedScreen<Screen extends AbstractScreen> extends AbstractScreen {

  protected final Screen gameScreen;

  private final ShapeRenderer renderer;

  protected final Stage stage;

  protected final Skin skin;

  public AbstractPausedScreen(ThingsThatWereGame game, World world, SpriteBatch batch, Screen gameScreen) {
    super(game, world, batch);
    this.gameScreen = gameScreen;
    this.stage = new Stage(new ScreenViewport(), batch);
    this.renderer = new ShapeRenderer();

    this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
  }

  abstract protected void initMenu();

  protected final void resumeGame() {
    game.setScreen(gameScreen);
  }

  @Override
  public void show() {
    super.show();

    initMenu();
    InputAdapter escape = new InputAdapter() {
      @Override
      public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE) {
          resumeGame();
          return true;
        } else
          return false;
      }
    };
    Gdx.input.setInputProcessor(new InputMultiplexer(escape, stage));
  }

  @Override
  public void render(float delta) {
    // don't want game screen animations to continue, just display as background
    gameScreen.render(0);

    // default rendering would draw a black background
    // super.render(delta);

    // draw a semi-transparent background
    Gdx.gl20.glEnable(GL20.GL_BLEND);
    renderer.setProjectionMatrix(camera.combined);
    renderer.begin(ShapeType.Filled);
    renderer.setColor(0, 0, 0, 0.5f);
    renderer.rect(-10, -10, Gdx.graphics.getWidth() + 20, Gdx.graphics.getHeight() + 20);
    renderer.end();

    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    gameScreen.resize(width, height);

    super.resize(width, height);

    stage.getViewport().update(width, height, true);
    /*
     * The GUI use a ScreenViewport, meaning it won't scale when screen size
     * change. This is fine because we don't want the GUI size to change,
     * becoming zoomed in and ugly or zoomed and unreadable. However it has a
     * small side effect: the existing menu were placed according to the
     * vertical stage size. The stage size changed with the screen (game window)
     * one. So we must recompute the GUI elements coordinates. The simplest way
     * to do it is to recreate the menu.
     */
    initMenu();
  }

  @Override
  public void dispose() {
    // do not dispose game screen when quitting this one

    super.dispose();

    stage.dispose();
  }

}
