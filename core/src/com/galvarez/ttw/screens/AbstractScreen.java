package com.galvarez.ttw.screens;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;

public abstract class AbstractScreen extends ScreenAdapter implements Screen {

  protected final ThingsThatWereGame game;

  protected final World world;

  protected final OrthographicCamera camera;

  protected final SpriteBatch batch;

  public AbstractScreen(ThingsThatWereGame game, World world, SpriteBatch batch) {
    this.game = game;
    this.world = world;
    camera = new OrthographicCamera();
    this.batch = batch;
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    camera.update();

    world.setDelta(delta);
    world.process();
  }

  @Override
  public void resize(int width, int height) {
    game.windowWidth = width;
    game.windowHeight = height;
  }

}
