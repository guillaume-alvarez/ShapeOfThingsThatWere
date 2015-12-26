package com.galvarez.ttw.model;

import static java.lang.Math.max;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.EventsSystem.EventHandler;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.FadingMessageRenderSystem;
import com.galvarez.ttw.rendering.IconsSystem.Type;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * This classes manages epidemics that can regularly happen to influence sources
 * when their power is too far above their growth.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class DisasterSystem extends EntitySystem implements EventHandler {

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Empire> empires;

  private ComponentMapper<MapPosition> positions;

  private NotificationsSystem notifications;

  private FadingMessageRenderSystem fadingSystem;

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public DisasterSystem(OverworldScreen screen) {
    super(Aspect.getAspectForAll(InfluenceSource.class));
    this.screen = screen;
  }

  @Override
  public String getType() {
    return "Disaster";
  }

  @Override
  protected void initialize() {
    super.initialize();

    world.getSystem(EventsSystem.class).addEventType(this);
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    // done from events
  }

  @Override
  public int getProgress(Entity e) {
    InfluenceSource source = sources.get(e);
    int unhealth = source.power() - source.health;
    return max(0, unhealth / 2);
  }

  @Override
  public boolean execute(Entity e) {
    InfluenceSource source = sources.get(e);
    source.setPower(source.health / 2);

    fadingSystem.createFadingIcon(Type.DISEASE, empires.get(e).color, positions.get(e), 3f);
    notifications.addNotification(() -> screen.select(e, false), null, Type.DISEASE, "Mortal disease strikes %s!",
        e.getComponent(Description.class));

    return true;
  }

}
