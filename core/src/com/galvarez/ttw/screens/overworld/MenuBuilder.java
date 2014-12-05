package com.galvarez.ttw.screens.overworld;

import static java.lang.Math.min;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.IntIntMap;
import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.NotificationsSystem.Notification;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.components.Sprite;
import com.galvarez.ttw.rendering.ui.FramedDialog;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.controls.InputManager;
import com.galvarez.ttw.screens.overworld.controls.MenuProcessor;

public class MenuBuilder {

  private static final Logger log = LoggerFactory.getLogger(MenuBuilder.class);

  private static final int MENU_PADDING = 15;

  private final MenuProcessor menuProcessor;

  private final Stage stage;

  private final Skin skin;

  private final FramedMenu turnMenu, empireMenu, notifMenu;

  private final FramedMenu selectionMenu;

  private FramedMenu actionMenu;

  private final World world;

  private final GameMap map;

  private final OverworldScreen screen;

  private final InputManager inputManager;

  private final NotificationsSystem notifications;

  public MenuBuilder(MenuProcessor menuProcessor, Stage stage, World world, GameMap map, OverworldScreen screen,
      InputManager inputManager) {
    this.menuProcessor = menuProcessor;
    this.stage = stage;
    this.world = world;
    this.map = map;
    this.screen = screen;
    this.inputManager = inputManager;
    this.notifications = screen.notificationsSystem;

    FileHandle skinFile = new FileHandle("resources/uiskin/uiskin.json");
    skin = new Skin(skinFile);

    turnMenu = new FramedMenu(skin, 256, 128);
    empireMenu = new FramedMenu(skin, 256, 256).nbColumns(1);
    selectionMenu = new FramedMenu(skin, 256, 512);
    notifMenu = new FramedMenu(skin, 400, 512);
  }

  public void buildTurnMenu() {
    turnMenu.clear();

    // EndTurn button
    turnMenu.addButton("End turn", null, () -> menuProcessor.endTurn(), screen.canFinishTurn());

    turnMenu.addToStage(stage, MENU_PADDING, stage.getHeight() - MENU_PADDING, false);
  }

  public void buildEmpireMenu() {
    empireMenu.clear();

    // here present a new screen with diplomatic relations
    empireMenu.addButton("Diplomacy", () -> menuProcessor.diplomacyMenu());

    // here present a new screen with army preferences
    Army army = screen.player.getComponent(Army.class);
    // TODO add a army menu when we know what to do with it
    // menuProcessor.armyMenu();
    empireMenu.addButton("Army (power=" + army.militaryPower + ")", null);

    // here present a sub-menu to see current discovery and be able to change it
    Discoveries discoveries = screen.player.getComponent(Discoveries.class);
    empireMenu.addButton("Discovery "
        + (discoveries != null && discoveries.next != null ? "(" + discoveries.next.progress + "%)" : "(NONE)"),
        () -> menuProcessor.discoveryMenu());

    // here present a new screen to choose policies
    Policies policies = screen.player.getComponent(Policies.class);
    empireMenu.addButton("Policies (stability " + policies.stability + "%)", () -> menuProcessor.policiesMenu());

    empireMenu.addToStage(stage, MENU_PADDING, turnMenu.getY() - MENU_PADDING, false);
  }

  public void buildSelectionMenu(final MapPosition tile, Entity e) {
    selectionMenu.clear();

    addTileDescription(tile);
    Influence influence = addInfluences(tile);

    if (e != null) {
      addDescription(e);
      InfluenceSource infSource = e.getComponent(InfluenceSource.class);
      if (infSource != null)
        selectionMenu
            .addLabel("Power: " + infSource.power + " (+"
                + (100 * infSource.powerAdvancement / screen.influenceSystem.getRequiredPowerAdvancement(infSource))
                + "%)");
    }

    Entity source = influence.getMainInfluenceSource(world);
    if (source != null) {
      Entity empire = source.getComponent(InfluenceSource.class).empire;
      if (empire != screen.player && screen.player != null)
        addEmpire(screen.player, empire);
    }

    selectionMenu.addToStage(stage, MENU_PADDING, empireMenu.getY() - MENU_PADDING, true);
  }

  private Influence addInfluences(MapPosition tile) {
    Influence influence = map.getInfluenceAt(tile);
    int mainSource = influence.getMainInfluenceSource();
    StringBuilder sb = new StringBuilder("Influence: ");
    for (IntIntMap.Entry e : influence) {
      // TODO use empire color when https://github.com/libgdx/libgdx/issues/1934
      // is fixed
      Entity source = world.getEntity(e.key);
      Empire empire = empire(source);
      // FIXME color [] flags seems not to be taken into account, whereas it
      // follows guidelines from
      // https://github.com/libgdx/libgdx/wiki/Color-Markup-Language
      sb.append("\n [#").append(empire.color.toString().toUpperCase()).append(']')
          .append(source.getComponent(Name.class).name).append("[]: ")
          .append(100 * e.value / InfluenceSystem.INITIAL_POWER).append('%');
      int delta = influence.getDelta(source);
      if (delta > 0)
        sb.append(" +").append(100 * delta / InfluenceSystem.INITIAL_POWER).append('%');
      else if (delta < 0)
        sb.append(' ').append(100 * delta / InfluenceSystem.INITIAL_POWER).append('%');
      // ignore == 0
      if (e.key == mainSource)
        sb.append(" (main)");
      sb.append("[]");
    }
    selectionMenu.addLabel(sb.toString());
    return influence;
  }

  private static Empire empire(Entity source) {
    return source.getComponent(InfluenceSource.class).empire.getComponent(Empire.class);
  }

  private void addTileDescription(MapPosition tile) {
    Terrain terrain = map.getTerrainAt(tile);
    selectionMenu.addLabelSprite(terrain.getDesc() + " (" + tile.x + ", " + tile.y + ")",
        screen.mapRenderer.getTexture(terrain), Color.WHITE);
  }

  private void addDescription(Entity e) {
    Description desc = e.getComponent(Description.class);
    if (desc != null) {
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

  private void addEmpire(Entity player, Entity selected) {
    selectionMenu.addLabel("Empire: " + selected.getComponent(Name.class).name);
    Diplomacy playerDiplo = player.getComponent(Diplomacy.class);
    Diplomacy selectedDiplo = selected.getComponent(Diplomacy.class);
    selectionMenu.addLabel(" relations are " + playerDiplo.getRelationWith(selected));
    selectionMenu.addButton(" we want ", playerDiplo.getProposalTo(selected).str,
        () -> displayDiplomaticActionMenu(selectionMenu, playerDiplo, selected), true);
    selectionMenu.addLabel(" they want " + selectedDiplo.getProposalTo(player).str);
  }

  private void displayDiplomaticActionMenu(FramedMenu parent, Diplomacy diplo, Entity target) {
    if (actionMenu != null)
      actionMenu.clear();
    actionMenu = new FramedMenu(skin, 256, 128, parent);
    boolean hasActions = false;
    for (Action action : screen.diplomaticSystem.getPossibleActions(diplo, target)) {
      hasActions = true;
      actionMenu.addButton(action.str, () -> {
        if (action != Action.NO_CHANGE)
          diplo.proposals.put(target, action);
        actionMenu.clear();
        inputManager.reloadMenus();
      });
    }
    if (!hasActions)
      actionMenu.addLabel("No possible actions!");

    actionMenu.addToStage(stage, parent.getX() + parent.getWidth(), parent.getY() + 10, true);
  }

  public void buildNotificationMenu() {
    List<Notification> notifs = notifications.getNotifications();
    notifMenu.clear();
    if (notifs.isEmpty())
      notifMenu.addLabel("No notifications");
    else
      for (Notification n : notifs) {
        notifMenu.addButtonSprite(n.type, n.msg, () -> {
          inputManager.reloadMenus();
          if (n.action != null)
            n.action.run();
        }, true);
      }
    notifMenu.addToStage(stage, Gdx.graphics.getWidth() - 400, min(512, notifMenu.getTable().getPrefHeight()), false);
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
