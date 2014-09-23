package com.galvarez.ttw.utils;

public final class FloatPair {

  public final float x, y;

  public FloatPair(float x, float y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FloatPair)
      return equals((FloatPair) obj);
    return false;
  }

  public boolean equals(FloatPair p) {
    return x == p.x && y == p.y;
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
