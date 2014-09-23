package com.galvarez.ttw.screens.overworld.controls;

import java.util.Map;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

public class OverworldFlagController extends InputAdapter {

  private final OrthographicCamera camera;

  private final OverworldScreen screen;

  private final InputManager inputManager;

  public OverworldFlagController(OrthographicCamera camera, OverworldScreen screen, InputManager inputManager) {
    this.camera = camera;
    this.screen = screen;
    this.inputManager = inputManager;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    final MapPosition coords = MapTools.window2world(Gdx.input.getX(), Gdx.input.getY(), camera);

    // Did they click within the movable range?
    Map<MapPosition, String> flaggable = screen.getHighlightedTiles();
    if (flaggable != null && flaggable.containsKey(coords)) {
      Entity source = inputManager.selectedEntity;
      screen.flag(source, coords.x, coords.y);
    }

    // Wherever they clicked, they are now done with the "flagging" aspect of
    // things
    screen.stopHighlighing();
    inputManager.removeInputSystems(this);

    return true;
  }
}
