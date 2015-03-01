package com.galvarez.ttw.rendering.components;

import java.util.function.Supplier;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public final class TextBox extends Component {

  public final Supplier<String> generator;

  public String text;

  public Texture texture;

  public Color color;

  public TextBox(Supplier<String> generator) {
    this.generator = generator;
  }

  @Override
  public String toString() {
    return text != null ? text : "<null>";
  }

}
