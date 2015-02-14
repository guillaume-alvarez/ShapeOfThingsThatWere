package com.galvarez.ttw.utils;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.galvarez.ttw.model.data.Empire;

/**
 * Utilities for colors.
 * 
 * @author Guillaume Alvarez
 */
public final class Colors {

  private Colors() {
  }

  /**
   * Compute the most contrasting black or white color.
   * 
   * @see http://www.w3.org/TR/AERT#color-contrast
   * @see http://24ways.org/2010/calculating-color-contrast/
   * @see http://en.wikipedia.org/wiki/YIQ
   */
  @SuppressWarnings("javadoc")
  public static Color contrast(Color c) {
    float yiq = ((c.r * 299f) + (c.g * 587f) + (c.b * 114f)) / 1000f;
    return (yiq >= 128f) ? Color.BLACK : Color.WHITE;

  }

  public static String markup(Entity empire) {
    return markup(empire.getComponent(Empire.class).color);
  }

  public static String markup(Color c) {
    return "[#" + c.toString().toUpperCase() + "]";
  }

}
