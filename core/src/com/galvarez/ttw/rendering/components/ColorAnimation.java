package com.galvarez.ttw.rendering.components;

import com.artemis.PooledComponent;

public final class ColorAnimation extends PooledComponent {

  public float redMin, redMax, redSpeed;

  public float greenMin, greenMax, greenSpeed;

  public float blueMin, blueMax, blueSpeed;

  public float alphaMin, alphaMax, alphaSpeed;

  public boolean redAnimate, greenAnimate, blueAnimate, alphaAnimate, repeat;

  public ColorAnimation() {
  }

  @Override
  protected void reset() {
    redMin = greenMin = blueMin = alphaMin = 0f;
    redMax = greenMax = blueMax = alphaMax = 1f;
    redSpeed = greenSpeed = blueSpeed = alphaSpeed = 1f;
    redAnimate = greenAnimate = blueAnimate = alphaAnimate = repeat = false;
  }

}
