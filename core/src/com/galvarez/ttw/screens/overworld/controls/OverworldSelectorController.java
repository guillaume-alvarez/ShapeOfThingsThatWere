package com.galvarez.ttw.screens.overworld.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.AIInfluenceSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

public final class OverworldSelectorController extends InputAdapter {

  private static final Logger log = LoggerFactory.getLogger(OverworldSelectorController.class);

  private final OrthographicCamera camera;

  private final World world;

  private final GameMap gameMap;

  private final InputManager inputManager;

  /**
   * We need a copy of the screen implementing this controller (which has a copy
   * of the Game delegating to it) so we can change screens based on users
   * making selections.
   */
  private final OverworldScreen screen;

  private final MenuProcessor menuProcessor;

  public OverworldSelectorController(OrthographicCamera camera, World world, GameMap gameMap, OverworldScreen screen,
      InputManager inputManager, MenuProcessor menuProcessor) {
    this.camera = camera;
    this.world = world;
    this.gameMap = gameMap;
    this.screen = screen;
    this.inputManager = inputManager;
    this.menuProcessor = menuProcessor;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Keys.SPACE) {
      if (screen.selectedTile != null)
        screen.cameraMovementSystem.move(screen.selectedTile.x, screen.selectedTile.y);
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
      log.info("Selected {}", coords);

      // user clicked on the map :-)
      screen.selectedTile = coords;
      EntityFactory.createClick(world, coords.x, coords.y, 0.1f, 4f).addToWorld();

      // Check the entityID of the tile they click on
      Entity entity = gameMap.getEntityAt(coords.x, coords.y);
      // Now select the current entity (may be null)
      inputManager.selectedEntity = entity;

      // Put up a menu for the selected entity
      inputManager.menuBuilder.buildSelectionMenu(coords, entity);

      if (entity != null && entity.getComponent(InfluenceSource.class) != null
      // player cannot control AI empires
          && entity.getComponent(AIControlled.class) == null) {
        screen.highlightFlagRange(entity);
        inputManager.prependInputSystems(inputManager.flag);
      } else {
        // Make sure we drop any of the highlighted tiles
        screen.stopHighlighing();
      }

      // Need to keep the focus on the map
      inputManager.stage.setScrollFocus(null);
      inputManager.stage.setKeyboardFocus(null);

      return true;
    }

    // If they didn't click on someone, we didn't process it
    return false;
  }

}
