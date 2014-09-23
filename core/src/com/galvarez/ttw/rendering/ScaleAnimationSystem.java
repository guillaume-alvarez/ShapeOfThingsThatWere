package com.galvarez.ttw.rendering;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.rendering.components.ScaleAnimation;
import com.galvarez.ttw.rendering.components.Sprite;

@Wire
public final class ScaleAnimationSystem extends EntityProcessingSystem {

  private ComponentMapper<ScaleAnimation> sa;

  private ComponentMapper<Sprite> sm;

  @SuppressWarnings("unchecked")
  public ScaleAnimationSystem() {
    super(Aspect.getAspectForAll(ScaleAnimation.class));
  }

  @Override
  protected void process(Entity e) {
    ScaleAnimation scaleAnimation = sa.get(e);
    if (scaleAnimation.active) {
      Sprite sprite = sm.get(e);

      sprite.scaleX += scaleAnimation.speed * world.getDelta();

      if (sprite.scaleX > scaleAnimation.max) {
        sprite.scaleX = scaleAnimation.max;
        scaleAnimation.active = false;
      } else if (sprite.scaleX < scaleAnimation.min) {
        sprite.scaleX = scaleAnimation.min;
        scaleAnimation.active = false;
      }

      sprite.scaleY = sprite.scaleX;

    }
  }

}
