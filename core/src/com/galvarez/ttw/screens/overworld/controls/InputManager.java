package com.galvarez.ttw.screens.overworld.controls;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.screens.overworld.MenuBuilder;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

public class InputManager {

  final Stage stage;

  private final OverworldSelectorController select;

  private final OverworldDragController drag;

  final OverworldFlagController flag;

  public final MenuBuilder menuBuilder;

  private final MenuProcessor menuProcessor;

  private final InputMultiplexer manager;

  private final OverworldScreen screen;

  Entity selectedEntity;

  public InputManager(OrthographicCamera camera, World world, OverworldScreen screen, Stage stage, GameMap map) {
    this.screen = screen;
    this.stage = stage;

    selectedEntity = null;

    menuProcessor = new MenuProcessor(screen, this, stage);

    select = new OverworldSelectorController(camera, world, map, screen, this, menuProcessor);
    drag = new OverworldDragController(camera);
    flag = new OverworldFlagController(camera, screen, this);

    manager = new InputMultiplexer(stage, drag, select);

    menuBuilder = new MenuBuilder(menuProcessor, stage, world, map, screen, this);

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
    menuBuilder.buildEmpireMenu();
    menuBuilder.buildTurnMenu();
    menuBuilder.buildNotificationMenu();
    if (screen.selectedTile != null)
      menuBuilder.buildSelectionMenu(screen.selectedTile, selectedEntity);

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

}
