package com.galvarez.ttw.screens.overworld.controls;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.rendering.CameraMovementSystem;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

public final class OverworldSelectorController extends InputAdapter {

  private final OrthographicCamera camera;

  private final GameMap gameMap;

  private final InputManager inputManager;

  /**
   * We need a copy of the screen implementing this controller (which has a copy
   * of the Game delegating to it) so we can change screens based on users
   * making selections.
   */
  private final OverworldScreen screen;

  private final MenuProcessor menuProcessor;

  private final CameraMovementSystem cameraSystem;

  public OverworldSelectorController(OrthographicCamera camera, World world, GameMap gameMap, OverworldScreen screen,
      InputManager inputManager, MenuProcessor menuProcessor) {
    this.camera = camera;
    this.gameMap = gameMap;
    this.screen = screen;
    this.inputManager = inputManager;
    this.menuProcessor = menuProcessor;
    this.cameraSystem = world.getSystem(CameraMovementSystem.class);
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Keys.SPACE) {
      if (screen.selectedTile != null)
        cameraSystem.move(screen.selectedTile.x, screen.selectedTile.y);
    } else if (keycode == Keys.ENTER) {
      menuProcessor.endTurn();
    } else if (keycode == Keys.ESCAPE) {
      menuProcessor.pauseMenu();
    }

    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    // Get the coordinates they clicked on
    Vector3 mousePosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    MapPosition coords = MapTools.window2world(mousePosition.x, mousePosition.y, camera);

    // in any case there is a tile
    if (gameMap.isOnMap(coords)) {
      // Check the entityID of the tile they click on
      Entity entity = gameMap.getEntityAt(coords.x, coords.y);
      inputManager.select(coords, entity, true);
      return true;
    }

    // If they didn't click on someone, we didn't process it
    return false;
  }

}
