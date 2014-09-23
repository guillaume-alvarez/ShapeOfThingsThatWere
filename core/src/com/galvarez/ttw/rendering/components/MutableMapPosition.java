package com.galvarez.ttw.rendering.components;

import com.artemis.PooledComponent;

public final class MutableMapPosition extends PooledComponent {

  public float x, y = 0.f;

  public MutableMapPosition() {
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }

  @Override
  protected void reset() {
    x = y = 0f;
  }
}
