package com.galvarez.ttw.model.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.MyMath;

public class MapTools {

  public static final int col_multiple = 34;

  public static final int row_multiple = 38;

  public static final String name = "hex";

  public static Array<MapPosition> getNeighbors(int x, int y, int n) {
    Array<MapPosition> coordinates = new Array<MapPosition>();
    int min;
    int myrow;
    for (int row = y - n; row < y + n + 1; row++) {
      min = MyMath.min(2 * (row - y + n), n, -2 * (row - y - n) + 1);
      for (int col = x - min; col < x + min + 1; col++) {
        if ((col < 0) || (col >= width()))
          continue;
        if (x == col && y == row)
          continue;
        else if (x % 2 == 0)
          myrow = 2 * y - row;
        else
          myrow = row;
        if ((myrow < 0) || (myrow >= height()))
          continue;
        coordinates.add(new MapPosition(col, myrow));
      }
    }
    return coordinates;
  }

  public static Array<MapPosition> getNeighbors(MapPosition pos) {
    return getNeighbors(pos.x, pos.y, 1);
  }

  public static MapPosition getNeighbor(Border border, int x, int y) {
    switch (border) {
      case BOTTOM:
        return new MapPosition(x, y - 1);
      case BOTTOM_LEFT:
        return new MapPosition(x - 1, y - ((x + 1) % 2));
      case BOTTOM_RIGHT:
        return new MapPosition(x + 1, y - ((x + 1) % 2));
      case TOP:
        return new MapPosition(x, y + 1);
      case TOP_LEFT:
        return new MapPosition(x - 1, y + (x % 2));
      case TOP_RIGHT:
        return new MapPosition(x + 1, y + (x % 2));
      default:
        throw new IllegalStateException("Unknown border=" + border + " for x=" + x + " y=" + y);
    }
  }

  public static int distance(int x0, int y0, int x1, int y1) {
    int dx = Math.abs(x1 - x0);
    int dy = Math.abs(y1 - y0);

    // The distance can be tricky, because of how the columns are shifted.
    // Different cases must be considered, because the dx and dy above
    // are not sufficient to determine distance.

    if ((dx) % 2 == 0) {
      // distance from even->even or odd->odd column
      // important to know since evens and odds are offset
      return MyMath.max(dx, dx / 2 + dy);
    }

    // Otherwise the distance must be even->odd
    else if (((x0 % 2 == 0) && (y0 > y1)) || ((x1 % 2 == 0) && (y1 > y0))) {
      // even on top
      return MyMath.max(dx, (dx - 1) / 2 + dy);
    }
    // otherwise odd must be on top
    return MyMath.max(dx, (dx + 1) / 2 + dy);
  }

  public static MapPosition window2world(float x, float y, OrthographicCamera camera) {
    Vector3 pos = new Vector3(x, y, 0);
    camera.unproject(pos);
    int posx = (int) ((pos.x - 6f) / col_multiple);
    int posy = (int) ((pos.y - (float) row_multiple * (posx % 2) / 2) / row_multiple);
    return new MapPosition(posx, posy);
  }

  public static MapPosition libgdx2world(float x, float y) {
    Vector3 pos = new Vector3(x, y, 0);
    int posx = (int) ((pos.x - 6f) / col_multiple);
    int posy = (int) ((pos.y - (float) row_multiple * (posx % 2) / 2) / row_multiple);
    return new MapPosition(posx, posy);
  }

  public static FloatPair world2window(float x, float y) {
    int x0 = (int) x;
    float dx = x - x0; // purely the decimal part

    float posX = 5.5f + (x + 0.5f) * col_multiple;
    float posY = row_multiple * (y + 0.5f + (x0 % 2) * (0.5f - dx / 2f) + (x0 + 1) % 2 * dx / 2f);

    return new FloatPair(posX, posY);
  }

  public static int width;

  public static int width() {
    return width;
  }

  public static int height;

  public static int height() {
    return height;
  }

  public static FloatPair getDirectionVector(int x1, int y1, int x2, int y2) {
    FloatPair tile1 = world2window(x1, y1);
    FloatPair tile2 = world2window(x2, y2);
    return new FloatPair(tile2.x - tile1.x, tile2.y - tile1.y);
  }

  public enum Border {
    BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, TOP_LEFT, TOP, TOP_RIGHT;
  }

  public static void main(String[] args) {
    Array<MapPosition> neighbors = getNeighbors(10, 10, 0);
    System.err.println(neighbors.size);
  }
}
