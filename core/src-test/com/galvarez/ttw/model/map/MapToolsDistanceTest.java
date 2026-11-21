package com.galvarez.ttw.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapToolsDistanceTest {

  @Test
  void distance_same_position() {
    MapPosition pos = new MapPosition(5, 5);
    assertEquals(0, MapTools.distance(pos, pos));
  }

  @Test
  void distance_adjacent_horizontal() {
    // Even column to adjacent odd column
    assertEquals(1, MapTools.distance(new MapPosition(0, 0), new MapPosition(1, 0)));
    // Odd column to adjacent even column
    assertEquals(1, MapTools.distance(new MapPosition(1, 0), new MapPosition(2, 0)));
  }

  @Test
  void distance_adjacent_vertical() {
    assertEquals(1, MapTools.distance(new MapPosition(0, 0), new MapPosition(0, 1)));
    assertEquals(1, MapTools.distance(new MapPosition(1, 0), new MapPosition(1, 1)));
  }

  @Test
  void distance_symmetric() {
    MapPosition a = new MapPosition(2, 3);
    MapPosition b = new MapPosition(7, 9);
    assertEquals(MapTools.distance(a, b), MapTools.distance(b, a));
  }

  @Test
  void distance_non_negative() {
    for (int x1 = 0; x1 < 10; x1++) {
      for (int y1 = 0; y1 < 10; y1++) {
        for (int x2 = 0; x2 < 10; x2++) {
          for (int y2 = 0; y2 < 10; y2++) {
            int d = MapTools.distance(new MapPosition(x1, y1), new MapPosition(x2, y2));
            assertTrue(d >= 0, "Distance should be non-negative for (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
          }
        }
      }
    }
  }

  @Test
  void distance_even_to_even_column() {
    // (0,0) to (2,0) — even to even, dx=2
    assertEquals(2, MapTools.distance(new MapPosition(0, 0), new MapPosition(2, 0)));
  }

  @Test
  void distance_diagonal() {
    // (0,0) to (1,1) — in this hex grid layout, diagonal is distance 2
    // because column 0 is even and column 1 is odd (offset row)
    assertEquals(2, MapTools.distance(new MapPosition(0, 0), new MapPosition(1, 1)));
  }
}
