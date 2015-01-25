package com.galvarez.ttw.screens.overworld.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.screens.overworld.MenuBuilder;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

public class InputManager {

  private static final Logger log = LoggerFactory.getLogger(InputManager.class);

  final Stage stage;

  private final OverworldSelectorController select;

  private final OverworldDragController drag;

  final OverworldFlagController flag;

  public final MenuBuilder menuBuilder;

  private final InputMultiplexer manager;

  private final OverworldScreen screen;

  private final World world;

  Entity selectedEntity;

  public InputManager(OrthographicCamera camera, World world, OverworldScreen screen, Stage stage, GameMap map) {
    this.world = world;
    this.screen = screen;
    this.stage = stage;

    selectedEntity = null;

    select = new OverworldSelectorController(camera, world, map, screen, this);
    drag = new OverworldDragController(camera);
    flag = new OverworldFlagController(camera, screen, this);

    manager = new InputMultiplexer(stage, drag, select);

    menuBuilder = new MenuBuilder(stage, world, map, screen, this);

    Gdx.input.setInputProcessor(manager);
  }

  public void setEnabled(boolean enabled) {
    if (enabled)
      Gdx.input.setInputProcessor(manager);
    else
      Gdx.input.setInputProcessor(null);
  }

  public void reloadMenus() {
    Actor keyboardFocus = stage.getKeyboardFocus();
    Actor scrollFocus = stage.getScrollFocus();

    menuBuilder.buildTurnMenu();
    menuBuilder.buildIndicationMenu();
    menuBuilder.buildEmpireMenu();
    menuBuilder.buildNotificationMenu();
    if (screen.selectedTile != null)
      menuBuilder.buildSelectionMenu(screen.selectedTile, selectedEntity);
    menuBuilder.buildBuildingsMenu(selectedEntity);

    stage.setKeyboardFocus(keyboardFocus);
    stage.setScrollFocus(scrollFocus);
  }

  public void appendInputSystems(InputProcessor ... processors) {
    for (int i = 0; i < processors.length; i++)
      manager.addProcessor(processors[i]);
  }

  public void setInputSystems(InputProcessor ... processors) {
    manager.setProcessors(new Array<InputProcessor>(processors));
  }

  public void prependInputSystems(InputProcessor ... processors) {
    Array<InputProcessor> arr = new Array<>();
    arr.addAll(processors);
    arr.addAll(manager.getProcessors());
    manager.setProcessors(arr);
  }

  public void removeInputSystems(InputProcessor ... processors) {
    for (int i = 0; i < processors.length; i++)
      manager.removeProcessor(processors[i]);
  }

  public void select(MapPosition coords, Entity entity, boolean flagIfMoveable) {
    // user clicked on the map :-)
    screen.selectedTile = coords;
    EntityFactory.createClick(world, coords.x, coords.y, 0.1f, 4f);

    // Now select the current entity (may be null)
    selectedEntity = entity;

    if (entity != null)
      log.info("Selected {}: {}", coords, entity.getComponent(Description.class));
    else
      log.info("Selected {}: no entity", coords);

    // Put up a menu for the selected entity
    menuBuilder.buildSelectionMenu(coords, entity);
    menuBuilder.buildBuildingsMenu(entity);

    if (flagIfMoveable && entity != null && entity.getComponent(Destination.class) != null
    // player cannot control AI empires
        && entity.getComponent(AIControlled.class) == null) {
      screen.highlightFlagRange(entity);
      prependInputSystems(flag);
    } else {
      // Make sure we drop any of the highlighted tiles
      screen.stopHighlighing();
    }

    // Need to keep the focus on the map
    stage.setScrollFocus(null);
    stage.setKeyboardFocus(null);
  }

}
