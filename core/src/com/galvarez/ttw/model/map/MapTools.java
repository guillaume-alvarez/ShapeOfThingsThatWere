package com.galvarez.ttw.model.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.MyMath;

public class MapTools {

  public static final int col_multiple = 34;

  public static final int row_multiple = 38;

  public enum Border {
    BOTTOM_RIGHT {
      @Override
      public MapPosition getNeighbor(int x, int y) {
        return new MapPosition(x + 1, y - ((x + 1) % 2));
      }
    },
    BOTTOM {
      @Override
      public MapPosition getNeighbor(int x, int y) {
        return new MapPosition(x, y - 1);
      }
    },
    BOTTOM_LEFT {
      @Override
      public MapPosition getNeighbor(int x, int y) {
        return new MapPosition(x - 1, y - ((x + 1) % 2));
      }
    },
    TOP_LEFT {
      @Override
      public MapPosition getNeighbor(int x, int y) {
        return new MapPosition(x - 1, y + (x % 2));
      }
    },
    TOP {
      @Override
      public MapPosition getNeighbor(int x, int y) {
        return new MapPosition(x, y + 1);
      }
    },
    TOP_RIGHT {
      @Override
      public MapPosition getNeighbor(int x, int y) {
        return new MapPosition(x + 1, y + (x % 2));
      }
    };

    abstract public MapPosition getNeighbor(int x, int y);

    public final MapPosition getNeighbor(MapPosition pos) {
      return getNeighbor(pos.x, pos.y);
    }
  }

  public static int distance(MapPosition p0, MapPosition p1) {
    int x0 = p0.x;
    int y0 = p0.y;
    int x1 = p1.x;
    int y1 = p1.y;

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

  public static FloatPair world2window(MapPosition pos) {
    int x = pos.x;
    int y = pos.y;

    float posX = 5.5f + (x + 0.5f) * col_multiple;
    float posY = row_multiple * (y + 0.5f + (x % 2) * 0.5f);

    return new FloatPair(posX, posY);
  }

}
