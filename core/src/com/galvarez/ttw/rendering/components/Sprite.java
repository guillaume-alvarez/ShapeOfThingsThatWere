package com.galvarez.ttw.rendering.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Sprite extends Component {

  public enum Layer {
    DEFAULT, BACKGROUND, ACTORS_1, ACTORS_2, ACTORS_3, PARTICLES;

    public int getLayerId() {
      return ordinal();
    }
  }

  public Color color = Color.WHITE;

  public TextureRegion region;

  public String name;

  public float scaleX, scaleY = 1f;

  public float rotation = 0f;

  public int x, y, width, height;

  public Layer layer = Layer.DEFAULT;

  public int index;

  public Sprite() {
  }

}
