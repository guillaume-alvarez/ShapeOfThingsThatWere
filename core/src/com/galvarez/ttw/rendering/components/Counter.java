package com.galvarez.ttw.rendering.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public final class Counter extends Component {

  public final Color textColor;

  public final Color backColor;

  public Texture background;

  public int value;

  public Counter(Color textColor, Color backColor, int value) {
    this.textColor = textColor;
    this.backColor = backColor;
    this.value = value;
  }

}
