package com.galvarez.ttw.model.map;

import com.badlogic.gdx.graphics.Color;

/**
 * List the different terrain types.
 * 
 * @author Guillaume Alvarez
 */
public enum Terrain {

  DEEP_WATER("deep water", Color.NAVY, 50, false),

  SHALLOW_WATER("shallow water", Color.BLUE, 30, false),

  DESERT("desert", Color.YELLOW, 50, false),

  PLAIN("plain", Color.GREEN, 10, true),

  GRASSLAND("grassland", new Color(0, 0.5f, 0, 1), 15, true),

  FOREST("forest", Color.OLIVE, 30, false),

  HILLS("hills", Color.ORANGE, 30, true),

  MOUNTAIN("mountain", Color.MAROON, 50, false),

  ARCTIC("arctic", Color.WHITE, 100, false);

  private final Color color;

  private final int moveCost;

  private final String desc;

  private final boolean canStart;

  private Terrain(String desc, Color color, int moveCost, boolean canStart) {
    this.desc = desc;
    this.moveCost = moveCost;
    this.color = color;
    this.canStart = canStart;
  }

  public Color getColor() {
    return color;
  }

  public boolean moveBlock() {
    return false;
  }

  public boolean canStart() {
    return canStart;
  }

  public int moveCost() {
    return moveCost;
  }

  public int getTexture() {
    return ordinal();
  }

  public String getDesc() {
    return desc;
  }

}
