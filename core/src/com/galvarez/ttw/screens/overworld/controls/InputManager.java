package com.galvarez.ttw.screens.overworld.controls;

import com.galvarez.ttw.utils.Assets;
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

  public InputManager(Assets assets, OrthographicCamera camera, World world, OverworldScreen screen, Stage stage, GameMap map) {
    this.world = world;
    this.screen = screen;
    this.stage = stage;

    selectedEntity = null;

    select = new OverworldSelectorController(camera, world, map, screen, this);
    drag = new OverworldDragController(camera);
    flag = new OverworldFlagController(camera, screen, this);

    manager = new InputMultiplexer(stage, drag, select);

    menuBuilder = new MenuBuilder(assets, stage, world, map, screen, this);

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

    menuBuilder.clearSubMenus();
    menuBuilder.buildTurnMenu();
    menuBuilder.buildIndicationMenu();
    menuBuilder.buildEmpireMenu();
    menuBuilder.buildCardsMenu();
    menuBuilder.buildNotificationMenu();
    menuBuilder.buildEventsMenu(selectedEntity);
    if (screen.selectedTile != null)
      menuBuilder.buildSelectionMenu(screen.selectedTile, selectedEntity);

    stage.setKeyboardFocus(keyboardFocus);
    stage.setScrollFocus(scrollFocus);
  }

  public void appendInputSystems(InputProcessor ... processors) {
    for (InputProcessor p : processors)
      manager.addProcessor(p);
  }

  public void setInputSystems(InputProcessor ... processors) {
    manager.setProcessors(new Array<>(processors));
  }

  public void prependInputSystems(InputProcessor ... processors) {
    Array<InputProcessor> arr = new Array<>();
    arr.addAll(processors);
    arr.addAll(manager.getProcessors());
    manager.setProcessors(arr);
  }

  public void removeInputSystems(InputProcessor ... processors) {
    for (InputProcessor p : processors)
      manager.removeProcessor(p);
  }

  public void select(MapPosition coords, Entity entity, boolean flagIfMoveable) {
    // user clicked on the map :-)
    screen.selectedTile = coords;
    EntityFactory.createClick(world, coords.x, coords.y, 0.1f, 4f);

    // Now select the current entity (may be null)
    selectedEntity = entity;

    if (entity != null)
      log.debug("Selected {}: {}", coords, entity.getComponent(Description.class));
    else
      log.debug("Selected {}: no entity", coords);

    // Put up a menu for the selected entity
    menuBuilder.clearSubMenus();
    menuBuilder.buildSelectionMenu(coords, entity);
    menuBuilder.buildEventsMenu(entity);

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
