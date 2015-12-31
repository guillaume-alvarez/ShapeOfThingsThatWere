package com.galvarez.ttw.rendering;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.artemis.annotations.Wire;
import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Store notifications until processed.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class NotificationsSystem extends VoidEntitySystem {

  /** Notifications registered for next turn. */
  private final List<Notification> notifications = new ArrayList<>();

  /** Notifications for current turn. */
  private final List<Notification> current = new ArrayList<>();

  private IconsSystem icons;

  public NotificationsSystem() {
  }

  @Override
  protected void processSystem() {
    current.clear();
    current.addAll(notifications);
    notifications.clear();
  }

  public List<Notification> getNotifications() {
    for (Iterator<Notification> i = current.iterator(); i.hasNext();) {
      Notification n = i.next();
      if (n.discard != null && n.discard.canDiscard())
        i.remove();
    }
    return Collections.unmodifiableList(current);
  }

  public void addNotification(Runnable action, Condition discard, IconsSystem.Type type, String msg, Object ... args) {
    if (args == null || args.length == 0)
      notifications.add(new Notification(icons.get(type), action, discard, msg));
    else
      notifications.add(new Notification(icons.get(type), action, discard, format(msg, args)));
  }

  @FunctionalInterface
  public interface Condition {
    boolean canDiscard();
  }

  public static final class Notification {

    public final String msg;

    public final Drawable type;

    public final Runnable action;

    private final Condition discard;

    public Notification(Drawable type, Runnable action, Condition discard, String msg) {
      this.type = type;
      this.action = action;
      this.discard = discard;
      this.msg = msg;
    }

  }

  public boolean canFinishTurn() {
    for (Notification n : current)
      if (n.discard != null && !n.discard.canDiscard())
        return false;
    return true;
  }

  public void discard(Notification n) {
    if (n.discard == null || n.discard.canDiscard())
      current.remove(n);
  }

}
