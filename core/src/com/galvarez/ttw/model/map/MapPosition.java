package com.galvarez.ttw.model.map;

import com.artemis.Component;

public final class MapPosition extends Component {

  public final int x, y;

  public MapPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public int hashCode() {
    return x + y * 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    else if (obj instanceof MapPosition) {
      MapPosition p = (MapPosition) obj;
      return x == p.x && y == p.y;
    }
    return false;
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
