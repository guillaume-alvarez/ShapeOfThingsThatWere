package com.galvarez.ttw.model.map;

import com.badlogic.gdx.graphics.Color;

/**
 * List the different terrain types.
 * 
 * @author Guillaume Alvarez
 */
public enum Terrain {

  DEEP_WATER("deep water", Color.NAVY, 50),

  SHALLOW_WATER("shallow water", Color.BLUE, 30),

  DESERT("desert", Color.YELLOW, 50),

  PLAIN("plain", Color.GREEN, 10),

  GRASSLAND("grassland", new Color(0, 0.5f, 0, 1), 15),

  FOREST("forest", Color.OLIVE, 30),

  HILLS("hills", Color.ORANGE, 30),

  MOUNTAIN("mountain", Color.MAROON, 50),

  ARCTIC("arctic", Color.WHITE, 100);

  private final Color color;

  private final int moveCost;

  private final String desc;

  private Terrain(String desc, Color color, int moveCost) {
    this.desc = desc;
    this.moveCost = moveCost;
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public boolean moveBlock() {
    return false;
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
