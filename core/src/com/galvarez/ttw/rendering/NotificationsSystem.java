package com.galvarez.ttw.rendering;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.galvarez.ttw.rendering.ui.FramedDialog;

/**
 * Store notifications until processed.
 * 
 * @author Guillaume Alvarez
 */
public final class NotificationsSystem extends VoidEntitySystem {

  private final List<String> titles = new ArrayList<>();

  private final List<String> notifications = new ArrayList<>();

  private final List<Runnable> listeners = new ArrayList<>();

  private final Stage stage;

  private final Skin skin;

  public NotificationsSystem(Stage stage) {
    this.stage = stage;

    this.skin = new Skin(new FileHandle("resources/uiskin/uiskin.json"));
  }

  @Override
  protected void processSystem() {
    for (int i = 0; i < titles.size(); i++) {
      FramedDialog dialog = new FramedDialog(skin, titles.get(i), notifications.get(i));
      dialog.addButton("OK", listeners.get(i));
      dialog.setKey(Keys.ENTER, null);
      dialog.addToStage(stage, 350, 200);
    }
    titles.clear();
    notifications.clear();
    listeners.clear();
  }

  public void addNotification(String title, String msg, Object ... args) {
    addNotification(null, title, msg, args);
  }

  public void addNotification(Runnable action, String title, String msg, Object ... args) {
    titles.add(title);
    listeners.add(action);
    if (args == null || args.length == 0)
      notifications.add(msg);
    else
      notifications.add(format(msg, args));
  }

}
