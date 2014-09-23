package com.galvarez.ttw.rendering;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.rendering.components.ColorAnimation;
import com.galvarez.ttw.rendering.components.Sprite;

@Wire
public final class ColorAnimationSystem extends EntityProcessingSystem {

  private ComponentMapper<ColorAnimation> cam;

  private ComponentMapper<Sprite> sm;

  @SuppressWarnings("unchecked")
  public ColorAnimationSystem() {
    super(Aspect.getAspectForAll(ColorAnimation.class, Sprite.class));
  }

  @Override
  protected void process(Entity e) {
    ColorAnimation c = cam.get(e);
    Sprite sprite = sm.get(e);

    if (c.alphaAnimate) {
      float a = sprite.color.a;
      a += c.alphaSpeed * world.getDelta();

      if (a > c.alphaMax) {
        a = c.alphaMax;
        if (c.repeat) {
          c.alphaSpeed = -c.alphaSpeed;
        } else {
          c.alphaAnimate = false;
        }
      }

      else if (a < c.alphaMin) {
        a = c.alphaMin;
        if (c.repeat) {
          c.alphaSpeed = -c.alphaSpeed;
        } else {
          c.alphaAnimate = false;
        }
      }
    }

    if (c.redAnimate) {
      float r = sprite.color.r;
      r += c.redSpeed * world.getDelta();

      if (r > c.redMax) {
        r = c.redMax;
        if (c.repeat) {
          c.redSpeed = -c.redSpeed;
        } else {
          c.redAnimate = false;
        }
      }

      else if (r < c.redMin) {
        r = c.redMin;
        if (c.repeat) {
          c.redSpeed = -c.redSpeed;
        } else {
          c.redAnimate = false;
        }
      }
    }

    if (c.greenAnimate) {
      float g = sprite.color.g;
      g += c.greenSpeed * world.getDelta();

      if (g > c.greenMax) {
        g = c.greenMax;
        if (c.repeat) {
          c.greenSpeed = -c.greenSpeed;
        } else {
          c.greenAnimate = false;
        }
      }

      else if (g < c.greenMin) {
        g = c.greenMin;
        if (c.repeat) {
          c.greenSpeed = -c.greenSpeed;
        } else {
          c.greenAnimate = false;
        }
      }
    }

    if (c.blueAnimate) {
      float b = sprite.color.b;
      b += c.blueSpeed * world.getDelta();

      if (b > c.blueMax) {
        b = c.blueMax;
        if (c.repeat) {
          c.blueSpeed = -c.blueSpeed;
        } else {
          c.blueAnimate = false;
        }
      }

      else if (b < c.blueMin) {
        b = c.blueMin;
        if (c.repeat) {
          c.blueSpeed = -c.blueSpeed;
        } else {
          c.blueAnimate = false;
        }
      }
    }

  }
}
