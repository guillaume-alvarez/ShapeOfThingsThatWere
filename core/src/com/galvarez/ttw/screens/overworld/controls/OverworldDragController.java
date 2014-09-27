package com.galvarez.ttw.screens.overworld.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class OverworldDragController extends InputAdapter {

  private boolean dragging;

  private final OrthographicCamera camera;

  public OverworldDragController(OrthographicCamera camera) {
    dragging = false;
    this.camera = camera;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    if (!dragging)
      dragging = true;
    Vector2 delta = new Vector2(-camera.zoom * Gdx.input.getDeltaX(), camera.zoom * Gdx.input.getDeltaY());
    camera.translate(delta);

    return true;
  }

  @Override
  public boolean scrolled(int amount) {
    if ((camera.zoom > 0.2f || amount == 1) && (camera.zoom < 8 || amount == -1))
      camera.zoom += amount * 0.1;
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (!dragging)
      return false;
    dragging = false;
    return true;
  }

}
