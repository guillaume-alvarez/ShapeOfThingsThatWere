package com.galvarez.ttw.screens.overworld;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;
import com.galvarez.ttw.model.DiplomaticSystem;
import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.EventsSystem.EventHandler;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.components.*;
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
import com.galvarez.ttw.utils.Assets;
import com.galvarez.ttw.utils.Assets.Icon;

import java.util.List;

import static com.galvarez.ttw.utils.Colors.markup;
import static java.lang.Math.min;
import static java.lang.String.format;

public class MenuBuilder {

  private static final int MENU_PADDING = 15;

  private final Stage stage;

  private final Skin skin;

  private final FramedMenu turnMenu, indicationMenu, empireMenu, notifMenu;

  private final FramedMenu selectionMenu;

  private final FramedMenu eventsMenu;

  private FramedMenu actionMenu, mapMenu;

  private final World world;

  private final GameMap map;

  private final OverworldScreen screen;

  private final InputManager inputManager;

  private final NotificationsSystem notifications;

  private final Assets assets;

  private final TooltipManager tooltips;

  public MenuBuilder(Assets assets, Stage stage, World world, GameMap map, OverworldScreen screen, InputManager inputManager) {
    this.stage = stage;
    this.world = world;
    this.map = map;
    this.screen = screen;
    this.inputManager = inputManager;
    this.notifications = world.getSystem(NotificationsSystem.class);
    this.assets = assets;

    this.tooltips = new TooltipManager();
    tooltips.initialTime = 0;
    tooltips.maxWidth = 300;
    tooltips.animations = false;

    skin = assets.getSkin();

    turnMenu = new FramedMenu(skin, 256, 256);
    indicationMenu = new FramedMenu(skin, 512, 512);
    empireMenu = new FramedMenu(skin, 256, 256).nbColumns(1);
    selectionMenu = new FramedMenu(skin, 256, 512);
    eventsMenu = new FramedMenu(skin, 256, 1024);
    notifMenu = new FramedMenu(skin, 400, 512);
  }

  public void clearSubMenus() {
    if (mapMenu != null) {
      mapMenu.clear();
      mapMenu = null;
    }
    if (actionMenu != null) {
      actionMenu.clear();
      actionMenu = null;
    }
  }

  public void buildTurnMenu() {
    turnMenu.clear();

    // EndTurn button
    turnMenu.addButton("End turn (year " + screen.getCurrentYear() + ")",//
        null, screen::endTurn, screen.canFinishTurn());

    // Access to score ladder
    Score score = screen.player.getComponent(Score.class);
    turnMenu.addButton("#" + score.rank + " " + score.totalScore + " (+" + score.lastTurnPoints + ")",
        screen::scoresMenu);

    // access to map options
    turnMenu.addButton("Map options", () -> displayMapMenu(turnMenu));

    turnMenu.addToStage(stage, MENU_PADDING, stage.getHeight() - MENU_PADDING, false);
  }

  private void displayMapMenu(FramedMenu parent) {
    if (mapMenu == null) {
      mapMenu = new FramedMenu(skin, 256, 128, turnMenu);
      mapMenu.clear();
      mapMenu.addCheckBox("Display colored influence?", screen.displayColoredInfluence(),
          screen::displayColoredInfluence);
      mapMenu.addToStage(stage, parent.getX() + parent.getWidth(), parent.getY() + 10, true);
    } else {
      mapMenu.clear();
      mapMenu = null;
    }
  }

  public void buildIndicationMenu() {
    indicationMenu.clear();

    List<String> indications = screen.getIndications();
    if (indications != null && !indications.isEmpty()) {
      // set width before for better layout
      indicationMenu.setWidth(stage.getWidth() - (turnMenu.getWidth() + eventsMenu.getWidth() + MENU_PADDING * 3));
      for (String i : indications)
        indicationMenu.addLabel(i);

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
    empireMenu.addButton("Discoveries", screen::discoveryMenu);

    // here present a new screen to choose policies
    int instability = screen.revoltSystem.getInstability(screen.player);
    if (instability <= 0)
      empireMenu.addButton("Policies (instability " + instability + ")", screen::policiesMenu);
    else {
      empireMenu.addButton(empireMenu.getSkin().get("colored", LabelStyle.class), //
          "[BLACK]Policies (instability [RED]" + instability + "[])", //
          null, screen::policiesMenu, true);
    }

    empireMenu.addToStage(stage, MENU_PADDING, turnMenu.getY() - MENU_PADDING, false);
  }

  public void buildSelectionMenu(final MapPosition tile, Entity e) {
    selectionMenu.clear();

    addTileDescription(tile);
    Influence influence = addInfluences(tile);

    if (e != null) {
      addDescription(e);
      InfluenceSource source = e.getComponent(InfluenceSource.class);
      if (source != null)
        selectionMenu.addLabel("Power: " + source.power() + " (" + number(source.growth / 10f) + "%/turn, health="
            + source.health + ")");
    }

    Entity source = influence.getMainInfluenceSource(world);
    if (source != null) {
      if (source != screen.player && screen.player != null)
        addEmpire(screen.player, source);
    }

    selectionMenu.addToStage(stage, MENU_PADDING, empireMenu.getY() - MENU_PADDING, true);
  }

  private static String number(int i) {
    return i >= 0 ? "+" + i : Integer.toString(i);
  }

  private static String number(float f) {
    return f >= 0 ? "+" + f : Float.toString(f);
  }

  private Influence addInfluences(MapPosition tile) {
    Influence influence = map.getInfluenceAt(tile);
    int mainSource = influence.getMainInfluenceSource();
    StringBuilder sb = new StringBuilder("[BLACK]Influence: ");
    // print first the main influence on the tile if there is one
    if (influence.hasMainInfluence()) {
      Entity source = influence.getMainInfluenceSource(world);
      addEntityInfluence(influence, sb, source, influence.getInfluence(source));
      sb.append(" (main)");
    }
    // else how much is required to control it
    else {
      sb.append("\n ")
              .append("No control, need ")
              .append(100 * influence.terrain.moveCost() / InfluenceSystem.INITIAL_POWER).append('%');
    }
    // then all other empires
    for (IntIntMap.Entry e : influence) {
      if (e.key != influence.getMainInfluenceSource()) {
        Entity source = world.getEntity(e.key);
        addEntityInfluence(influence, sb, source, e.value);
      }
    }
    selectionMenu.addColoredLabel(sb.toString());
    return influence;
  }

  private void addEntityInfluence(Influence influence, StringBuilder sb, Entity source, int score) {
    Empire empire = source.getComponent(Empire.class);
    sb.append("\n ").append(markup(empire.color)).append(source.getComponent(Name.class).name).append("[]: ")
            .append(100 * score / InfluenceSystem.INITIAL_POWER).append('%').append(' ')
            .append(number(100 * influence.getDelta(source) / InfluenceSystem.INITIAL_POWER)).append('%');
  }

  private void addTileDescription(MapPosition tile) {
    Terrain terrain = map.getTerrainAt(tile);
    String label = terrain.getDesc() + " (" + tile.x + ", " + tile.y + ")";
    selectionMenu.addLabelSprite(label, screen.mapRenderer.getTexture(terrain), Color.WHITE);
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
    if (actionMenu == null) {
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
    } else {
      actionMenu.clear();
      actionMenu = null;
    }
  }

  public void buildEventsMenu(Entity e) {
    eventsMenu.clear();

    if (e == null || e.getComponent(EventsCount.class) == null)
      e = screen.player;

    EventsCount events = e.getComponent(EventsCount.class);
    if (events != null) {
      eventsMenu.addLabel("Possible events in " + e.getComponent(Name.class));
      for (Entry<EventHandler> evt : events.display) {
        Label l = eventsMenu.addLabel(format(" %s: %d (+%d)", evt.key.getType(), evt.value, events.increment.get(evt.key, 0)));
        String reason = events.reasons.get(evt.key);
        if (reason != null) {
          FramedMenu tooltip = new FramedMenu(skin, 256, 256);
          tooltip.addLabel(reason);
          l.addListener(new Tooltip(tooltip.buildMenu(), tooltips));
        }
      }

      eventsMenu.addToStage(stage, stage.getWidth() - 256, stage.getHeight() - MENU_PADDING, false);
    }
  }

  public void buildNotificationMenu() {
    List<Notification> notifs = notifications.getNotifications();
    boolean canEndTurn = true; // can always end if there is no notification
    notifMenu.clear();

    for (Notification n : notifs) {
      canEndTurn &= !n.requiresAction();
      Button b = notifMenu.addButtonSprite(n.type, n.msg, () -> {
        inputManager.reloadMenus();
        if (n.action != null)
          n.action.run();
      }, true);
      b.addListener(new ClickListener(Buttons.RIGHT) {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          notifications.discard(n);
          inputManager.reloadMenus();
        }
      });
    }

    // let user end turn from the same point as notifications
    if (canEndTurn) {
      notifMenu.addButtonSprite(assets.getDrawable(Icon.END_TURN), "End turn (year " + screen.getCurrentYear() + ")",
              screen::endTurn, screen.canFinishTurn());
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
