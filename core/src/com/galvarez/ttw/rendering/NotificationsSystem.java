package com.galvarez.ttw.rendering;

import com.artemis.annotations.Wire;
import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.galvarez.ttw.utils.Assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.format;

/**
 * Store notifications until processed.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class NotificationsSystem extends VoidEntitySystem {

  private static final Comparator<Notification> INFO_FIRST = new Comparator<Notification>() {
    @Override
    public int compare(Notification o1, Notification o2) {
      if (o1.requiresAction()) {
        if (o2.requiresAction())
          return o1.msg.compareTo(o2.msg);
        else
          return 1;
      } else {
        if (o2.requiresAction()) return -1;
        else
          return o1.msg.compareTo(o2.msg);
      }
    }
  };

  /** Notifications registered for next turn. */
  private final List<Notification> notifications = new ArrayList<>();

  /** Notifications for current turn. */
  private final List<Notification> current = new ArrayList<>();

  private final Assets icons;

  public NotificationsSystem(Assets icons) {
    this.icons = icons;
  }

  @Override
  protected void processSystem() {
    current.clear();
    current.addAll(notifications);
    notifications.clear();
  }

  public List<Notification> getNotifications() {
    current.removeIf(n -> n.discard != null && n.discard.canDiscard());
    current.sort(INFO_FIRST);
    return Collections.unmodifiableList(current);
  }

  public void addNotification(Runnable action, Condition discard, Assets.Icon type, String msg, Object ... args) {
    if (args == null || args.length == 0)
      notifications.add(new Notification(icons.getDrawable(type), action, discard, msg));
    else
      notifications.add(new Notification(icons.getDrawable(type), action, discard, format(msg, args)));
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

    public boolean requiresAction() {
      return discard != null && !discard.canDiscard();
    }

  }

  public boolean canFinishTurn() {
    for (Notification n : current)
      if (n.requiresAction())
        return false;
    return true;
  }

  public void discard(Notification n) {
    if (!n.requiresAction())
      current.remove(n);
  }

}
