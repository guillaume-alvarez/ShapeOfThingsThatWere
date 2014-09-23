package com.galvarez.ttw.rendering.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class SpriteAnimation extends Component {

  public Animation animation;

  public float stateTime = 0f;

  public float frameDuration = 0.1f;

  public PlayMode playMode = PlayMode.NORMAL;

  public SpriteAnimation() {
  }

  public TextureRegion getFrame() {
    return animation.getKeyFrame(stateTime);
  }

}
