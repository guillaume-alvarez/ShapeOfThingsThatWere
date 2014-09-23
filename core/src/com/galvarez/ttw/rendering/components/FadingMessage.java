package com.galvarez.ttw.rendering.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;

public final class FadingMessage extends PooledComponent {

  public String label;

  public float duration, currentTime;

  public float vx, vy;

  public Color color;

  public FadingMessage() {
  }

  @Override
  public void reset() {
    label = null;
    color = null;
    duration = currentTime = vx = vy = 0f;
  }

}
