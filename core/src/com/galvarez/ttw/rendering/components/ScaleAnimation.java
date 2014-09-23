package com.galvarez.ttw.rendering.components;

import com.artemis.PooledComponent;

public final class ScaleAnimation extends PooledComponent {

  public float min, max, speed;

  public boolean repeat, active;

  public ScaleAnimation() {
  }

  @Override
  public void reset() {
    this.speed = 0f;
    this.min = 0f;
    this.max = 100f;
    this.repeat = false;
    this.active = true;
  }

}
