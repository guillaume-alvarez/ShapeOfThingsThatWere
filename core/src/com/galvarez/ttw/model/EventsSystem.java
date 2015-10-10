package com.galvarez.ttw.model;

import java.util.ArrayList;
import java.util.List;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.components.EventsCount;

@Wire
public final class EventsSystem extends EntitySystem {

  public interface EventHandler {

    /** Get the score progress toward next event of this type. */
    int getProgress(Entity e);

    /** Execute the event on the entity. */
    void execute(Entity e);

  }

  private ComponentMapper<EventsCount> counts;

  private final List<EventHandler> types = new ArrayList<>();

  public EventsSystem() {
    super(Aspect.getAspectForAll(EventsCount.class));
  }

  public void addEventType(EventHandler type) {
    types.add(type);
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    for (EventHandler type : types)
      counts.get(e).scores.put(type, 0);
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    int maxScore = Integer.MIN_VALUE;
    EventHandler selected = null;
    for (Entity e : entities) {
      EventsCount count = counts.get(e);
      for (EventHandler type : count.scores.keys()) {
        int newScore = type.getProgress(e) + count.scores.get(type, 0);
        count.scores.put(type, newScore);
        if (newScore > maxScore) {
          selected = type;
          maxScore = newScore;
        }
      }
      if (selected != null) {
        selected.execute(e);
        // also reset score
        count.scores.put(selected, 0);
      }
    }
  }
}
