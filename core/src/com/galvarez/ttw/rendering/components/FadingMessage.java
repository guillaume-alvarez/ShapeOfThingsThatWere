package com.galvarez.ttw.rendering.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class FadingMessage extends PooledComponent {

  public String label;

  public TextureRegion icon;

  public float duration, currentTime;

  public float vx, vy;

  public Color color;

  public FadingMessage() {
  }

  @Override
  public void reset() {
    label = null;
    icon = null;
    color = null;
    duration = currentTime = vx = vy = 0f;
  }

}
