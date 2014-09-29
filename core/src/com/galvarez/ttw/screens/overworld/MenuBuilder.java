package com.galvarez.ttw.screens.overworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.IntIntMap;
import com.galvarez.ttw.model.AIInfluenceSystem;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.components.Sprite;
import com.galvarez.ttw.rendering.ui.FramedDialog;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.controls.MenuProcessor;

public class MenuBuilder {

  private static final Logger log = LoggerFactory.getLogger(MenuBuilder.class);

  private static final int MENU_PADDING = 15;

  private final MenuProcessor menuProcessor;

  private final Stage stage;

  private final Skin skin;

  private final FramedMenu turnMenu, empireMenu;

  private final FramedMenu selectionMenu;

  private final World world;

  private final GameMap map;

  private final OverworldScreen screen;

  public MenuBuilder(MenuProcessor menuProcessor, Stage stage, World world, GameMap map, OverworldScreen screen) {
    this.menuProcessor = menuProcessor;
    this.stage = stage;
    this.world = world;
    this.map = map;
    this.screen = screen;

    FileHandle skinFile = new FileHandle("resources/uiskin/uiskin.json");
    skin = new Skin(skinFile);

    turnMenu = new FramedMenu(skin, 256, 128);
    empireMenu = new FramedMenu(skin, 256, 256).nbColumns(1);
    selectionMenu = new FramedMenu(skin, 256, 512);
  }

  public void buildTurnMenu() {
    turnMenu.clear();

    // EndTurn button
    ChangeListener turnListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        menuProcessor.endTurn();
      }
    };
    turnMenu.addButton("End turn", turnListener, true);

    turnMenu.addToStage(stage, MENU_PADDING, stage.getHeight() - MENU_PADDING, false);
  }

  public void buildEmpireMenu() {
    empireMenu.clear();

    // here present a new screen with diplomatic relations
    empireMenu.addButton("Diplomacy", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        menuProcessor.diplomacyMenu();
      }
    }, true);

    // here present a sub-menu to see current discovery and be able to change it
    Discoveries discoveries = screen.player.getComponent(Discoveries.class);
    empireMenu.addButton("Discovery "
        + (discoveries != null && discoveries.next != null ? "(" + discoveries.next.progress + "%)" : "(NONE)"),
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            menuProcessor.discoveryMenu();
          }
        }, true);

    empireMenu.addToStage(stage, MENU_PADDING, turnMenu.getY() - MENU_PADDING, false);
  }

  public void buildSelectionMenu(final MapPosition tile, Entity e) {
    selectionMenu.clear();

    addTileDescription(tile);
    addInfluences(tile);

    if (e != null) {
      addDescription(e);
      InfluenceSource infSource = e.getComponent(InfluenceSource.class);
      if (infSource != null)
        selectionMenu
            .addLabel("Power: " + infSource.power + " (+"
                + (100 * infSource.powerAdvancement / screen.influenceSystem.getRequiredPowerAdvancement(infSource))
                + "%)");
    }

    selectionMenu.addToStage(stage, MENU_PADDING, empireMenu.getY() - MENU_PADDING, true);
  }

  private void addInfluences(MapPosition tile) {
    Influence influence = map.getInfluenceAt(tile);
    int mainSource = influence.getMainInfluenceSource();
    StringBuilder sb = new StringBuilder("Influence: ");
    for (IntIntMap.Entry e : influence) {
      // TODO use empire color when https://github.com/libgdx/libgdx/issues/1934
      // is fixed
      Entity source = world.getEntity(e.key);
      sb.append("\n ").append(source.getComponent(Name.class).name).append(": ")
          .append(100 * e.value / InfluenceSystem.INITIAL_POWER).append('%');
      int delta = influence.getDelta(source);
      if (delta > 0)
        sb.append(" +").append(100 * delta / InfluenceSystem.INITIAL_POWER).append('%');
      else if (delta < 0)
        sb.append(' ').append(100 * delta / InfluenceSystem.INITIAL_POWER).append('%');
      // ignore == 0
      if (e.key == mainSource)
        sb.append(" (main)");
    }
    selectionMenu.addLabel(sb.toString());
  }

  private void addTileDescription(MapPosition tile) {
    Terrain terrain = map.getTerrainAt(tile);
    selectionMenu.addLabelSprite(terrain.getDesc() + " (" + tile.x + ", " + tile.y + ")",
        screen.mapRenderer.getTexture(terrain), Color.WHITE);
  }

  private void addDescription(Entity e) {
    Description desc = e.getComponent(Description.class);
    if (desc != null) {
      log.info("Selected {}", desc.desc);

      if (desc.texture != null) {
        selectionMenu.addLabelSprite(desc.desc, desc.texture, Color.WHITE);
      } else {
        Sprite sprite = e.getComponent(Sprite.class);
        if (sprite != null)
          selectionMenu.addLabelSprite(desc.desc, sprite.region, sprite.color);
        else
          selectionMenu.addLabel(desc.desc);
      }
    }
  }

  public void buildDialog(String title, int minWidth, int minHeight, String message, Button ... buttons) {
    FramedDialog fd = new FramedDialog(skin, title, message);
    for (Button b : buttons) {
      b.align(Align.center);
      fd.addButton(b);
    }
    fd.addToStage(stage, minWidth, minHeight);
  }

  public TextButton getTextButton(String text, ChangeListener listener) {
    TextButton button = new TextButton(text, skin);
    if (listener != null)
      button.addListener(listener);
    return button;
  }

}
