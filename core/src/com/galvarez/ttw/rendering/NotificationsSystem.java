package com.galvarez.ttw.rendering;

import static java.lang.Math.min;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * Store notifications until processed.
 * 
 * @author Guillaume Alvarez
 */
public final class NotificationsSystem extends VoidEntitySystem {

  private static final int MENU_MAX_HEIGHT = 512;

  private static final int MENU_WIDTH = 400;

  private static final Logger log = LoggerFactory.getLogger(NotificationsSystem.class);

  public enum Type {
    DISCOVERY, DIPLOMACY;
  }

  private final List<Type> types = new ArrayList<>();

  private final List<String> notifications = new ArrayList<>();

  private final List<Runnable> listeners = new ArrayList<>();

  private final EnumMap<Type, Drawable> icons;

  private final Stage stage;

  private final Skin skin;

  private final FramedMenu menu;

  public NotificationsSystem(Stage stage) {
    this.stage = stage;

    this.skin = new Skin(new FileHandle("resources/uiskin/uiskin.json"));
    this.menu = new FramedMenu(skin, MENU_WIDTH, MENU_MAX_HEIGHT);

    this.icons = new EnumMap<>(Type.class);
    icons.put(Type.DISCOVERY, skin.getDrawable("discovery-bulb"));
    icons.put(Type.DIPLOMACY, skin.getDrawable("diplomacy-handshake"));
  }

  @Override
  protected void processSystem() {
    menu.clear();
    if (types.isEmpty())
      menu.addLabel("No notifications");
    else
      for (int i = 0; i < types.size(); i++)
        menu.addButtonSprite(icons.get(types.get(i)), notifications.get(i), listeners.get(i), true);
    menu.addToStage(stage, Gdx.graphics.getWidth() - 400, min(512, menu.getTable().getPrefHeight()), false);

    types.clear();
    notifications.clear();
    listeners.clear();
  }

  public void addNotification(Runnable action, Type type, String msg, Object ... args) {
    types.add(type);
    listeners.add(action);
    if (args == null || args.length == 0)
      notifications.add(msg);
    else
      notifications.add(format(msg, args));
  }

  public void reload() {
    // TODO recreate menu when screen size changes or some action was performed by user
  }

}
