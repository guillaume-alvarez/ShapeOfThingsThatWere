package com.galvarez.ttw.rendering;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.galvarez.ttw.rendering.components.Sprite;
import com.galvarez.ttw.rendering.components.SpriteAnimation;

@Wire
public final class SpriteAnimationSystem extends EntityProcessingSystem {

  private ComponentMapper<Sprite> sm;

  private ComponentMapper<SpriteAnimation> sam;

  @SuppressWarnings("unchecked")
  public SpriteAnimationSystem() {
    super(Aspect.getAspectForAll(Sprite.class, SpriteAnimation.class));
  }

  @Override
  protected void process(Entity e) {
    Sprite sprite = sm.get(e);
    SpriteAnimation anim = sam.get(e);

    anim.stateTime += world.getDelta();

    TextureRegion region = anim.getFrame();
    sprite.x = region.getRegionX();
    sprite.y = region.getRegionY();
    sprite.width = region.getRegionWidth();
    sprite.height = region.getRegionHeight();
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }
}
