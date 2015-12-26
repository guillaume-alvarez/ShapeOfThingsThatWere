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

    /**
     * Execute the event on the entity.
     * 
     * @return true if it could execute the event (false if some system
     *         condition prevented it)
     */
    boolean execute(Entity e);

    /** A pretty printing name for the event. */
    String getType();

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
    EventsCount count = counts.get(e);
    for (EventHandler type : types) {
      count.scores.put(type, 0);
    }
    count.increment.putAll(count.scores);
    count.display.putAll(count.scores);
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity e : entities)
      checkEvents(e);
  }

  private void checkEvents(Entity e) {
    int maxScore = Integer.MIN_VALUE;
    EventHandler selected = null;
    EventsCount count = counts.get(e);
    for (EventHandler type : count.scores.keys()) {
      int progress = type.getProgress(e);
      int newScore = progress + count.scores.get(type, 0);
      count.increment.put(type, progress);
      count.display.put(type, newScore);
      count.scores.put(type, newScore);
      if (newScore > maxScore) {
        selected = type;
        maxScore = newScore;
      }
    }
    if (selected != null) {
      if (selected.execute(e))
        // also reset score
        count.scores.put(selected, 0);
    }
  }
}
