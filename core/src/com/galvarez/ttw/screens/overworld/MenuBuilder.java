package com.galvarez.ttw.screens.overworld;

import static java.lang.Math.min;

import java.util.List;
import java.util.Map.Entry;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.IntIntMap;
import com.galvarez.ttw.model.DiplomaticSystem;
import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.Buildings;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.components.Score;
import com.galvarez.ttw.model.data.Building;
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

public class MenuBuilder {

  private static final int MENU_PADDING = 15;

  private final Stage stage;

  private final Skin skin;

  private final FramedMenu turnMenu, indicationMenu, empireMenu, notifMenu;

  private final FramedMenu selectionMenu;

  private final FramedMenu buildingsMenu;

  private FramedMenu actionMenu;

  private final World world;

  private final GameMap map;

  private final OverworldScreen screen;

  private final InputManager inputManager;

  private final NotificationsSystem notifications;

  public MenuBuilder(Stage stage, World world, GameMap map, OverworldScreen screen, InputManager inputManager) {
    this.stage = stage;
    this.world = world;
    this.map = map;
    this.screen = screen;
    this.inputManager = inputManager;
    this.notifications = world.getSystem(NotificationsSystem.class);

    skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

    turnMenu = new FramedMenu(skin, 256, 256);
    indicationMenu = new FramedMenu(skin, 512, 512);
    empireMenu = new FramedMenu(skin, 256, 256).nbColumns(1);
    selectionMenu = new FramedMenu(skin, 256, 512);
    buildingsMenu = new FramedMenu(skin, 256, 1024);
    notifMenu = new FramedMenu(skin, 400, 512);
  }

  public void buildTurnMenu() {
    turnMenu.clear();

    // EndTurn button
    turnMenu.addButton("End turn (year " + screen.getCurrentYear() + ")",//
        null, screen::endTurn, screen.canFinishTurn());

    // Access to score ladder
    Score score = screen.player.getComponent(Score.class);
    turnMenu.addButton(score.totalScore + " (+" + score.lastTurnPoints + ")", screen::scoresMenu);

    turnMenu.addToStage(stage, MENU_PADDING, stage.getHeight() - MENU_PADDING, false);
  }

  public void buildIndicationMenu() {
    indicationMenu.clear();

    List<String> indications = screen.getIndications();
    if (indications != null && !indications.isEmpty()) {
      for (String i : indications)
        indicationMenu.addLabel(i);

      indicationMenu.setWidth(stage.getWidth() - (turnMenu.getWidth() + buildingsMenu.getWidth() + MENU_PADDING * 3));
      indicationMenu.addToStage(stage, MENU_PADDING + turnMenu.getX() + turnMenu.getWidth(), stage.getHeight()
          - MENU_PADDING, false);
    }
  }

  public void buildEmpireMenu() {
    empireMenu.clear();

    empireMenu.addLabel("- " + screen.player.getComponent(Name.class).name + " -");

    // here present a new screen with diplomatic relations
    empireMenu.addButton("Diplomacy", screen::diplomacyMenu);

    // here present a new screen with army preferences
    ArmyCommand command = screen.player.getComponent(ArmyCommand.class);
    empireMenu.addButton("Army (power=" + (command.militaryPower - command.usedPower) + "/" + command.militaryPower
        + ")", screen::armiesMenu);

    // here present a sub-menu to see current discovery and be able to change it
    Discoveries discoveries = screen.player.getComponent(Discoveries.class);
    empireMenu.addButton("Discovery "
        + (discoveries != null && discoveries.next != null ? "(" + discoveries.next.progress + "%)" : "(NONE)"),
        screen::discoveryMenu);

    // here present a new screen to choose policies
    Policies policies = screen.player.getComponent(Policies.class);
    int stability = policies.stability - screen.player.getComponent(InfluenceSource.class).power;
    if (stability >= 0)
      empireMenu.addButton("Policies (stability +" + stability + ")", screen::policiesMenu);
    else
      empireMenu.addButton("Policies (stability [RED]" + stability + "[])", screen::policiesMenu);

    empireMenu.addToStage(stage, MENU_PADDING, turnMenu.getY() - MENU_PADDING, false);
  }

  public void buildSelectionMenu(final MapPosition tile, Entity e) {
    selectionMenu.clear();

    addTileDescription(tile);
    Influence influence = addInfluences(tile);

    if (e != null) {
      addDescription(e);
      InfluenceSource infSource = e.getComponent(InfluenceSource.class);
      if (infSource != null) {
        int percent = 100 * infSource.powerAdvancement
            / world.getSystem(InfluenceSystem.class).getRequiredPowerAdvancement(infSource);
        selectionMenu.addLabel("Power: " + infSource.power + " (+" + percent + "%)");
      }
    }

    Entity source = influence.getMainInfluenceSource(world);
    if (source != null) {
      if (source != screen.player && screen.player != null)
        addEmpire(screen.player, source);
    }

    selectionMenu.addToStage(stage, MENU_PADDING, empireMenu.getY() - MENU_PADDING, true);
  }

  private Influence addInfluences(MapPosition tile) {
    Influence influence = map.getInfluenceAt(tile);
    int mainSource = influence.getMainInfluenceSource();
    StringBuilder sb = new StringBuilder("[BLACK]Influence: ");
    for (IntIntMap.Entry e : influence) {
      Entity source = world.getEntity(e.key);
      Empire empire = source.getComponent(Empire.class);
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
    }
    selectionMenu.addColoredLabel(sb.toString());
    return influence;
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
    for (Action action : world.getSystem(DiplomaticSystem.class).getPossibleActions(diplo, target)) {
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

  public void buildBuildingsMenu(Entity e) {
    buildingsMenu.clear();

    if (e != null) {
      Buildings buildings = e.getComponent(Buildings.class);
      if (buildings != null) {
        buildingsMenu.addLabel("Buildings in " + e.getComponent(Name.class));
        if (buildings.built.isEmpty())
          buildingsMenu.addLabel("- no buildings -");
        else
          for (Entry<String, Building> b : buildings.built.entrySet())
            buildingsMenu.addLabel(" " + b.getValue().getName() + " (" + b.getKey() + ")");

        buildingsMenu.addToStage(stage, stage.getWidth() - 256, stage.getHeight() - MENU_PADDING, false);
      }
    }
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
