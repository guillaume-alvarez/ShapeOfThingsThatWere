package com.galvarez.ttw.model;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
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
public final class DisasterSystem extends EntitySystem {

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
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    Entity target = getDisasterTarget(entities);
    if (target != null)
      createDisaster(target);
  }

  /** Return the entity targeted by a disaster this turn, null if none is. */
  private Entity getDisasterTarget(ImmutableBag<Entity> entities) {
    Array<Entity> empires = new Array<>(entities.size() / 2);
    IntArray chances = new IntArray(entities.size() / 2);
    int total = 0;

    for (Entity e : entities) {
      InfluenceSource source = sources.get(e);
      int unhealth = source.power() - source.health;
      if (unhealth > 0) {
        empires.add(e);
        chances.add(unhealth);
        total += unhealth;
      }
    }

    if (total > 100 || MathUtils.random(100) > total) {
      int target = MathUtils.random(total);
      int counter = 0;
      for (int i = 0; i < chances.size; i++) {
        counter += chances.get(i);
        if (counter > target)
          return empires.get(i);
      }
    }

    return null;
  }

  /** Apply the disaster effect and notify player. */
  private void createDisaster(Entity target) {
    InfluenceSource source = sources.get(target);
    source.setPower(source.health / 2);

    fadingSystem.createFadingIcon(Type.DISEASE, empires.get(target).color, positions.get(target), 3f);
    notifications.addNotification(() -> screen.select(target, false), null, Type.DISEASE, "Mortal disease strikes %s!",
        target.getComponent(Description.class));
  }
}
