package com.galvarez.ttw.model.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for hex grid neighbor calculation.
 * The hex grid uses offset coordinates where even and odd columns
 * have different neighbor patterns.
 */
@DisplayName("Approach A: Pure logic tests - Hex grid neighbor calculation")
class MapToolsBorderTest {

  @Test
  void even_column_top_neighbor() {
    // Column 0 is even, top neighbor should increase y
    MapPosition neighbor = MapTools.Border.TOP.getNeighbor(0, 5);
    assertEquals(0, neighbor.x);
    assertEquals(6, neighbor.y);
  }

  @Test
  void odd_column_top_neighbor() {
    // Column 1 is odd, top neighbor should increase y
    MapPosition neighbor = MapTools.Border.TOP.getNeighbor(1, 5);
    assertEquals(1, neighbor.x);
    assertEquals(6, neighbor.y);
  }

  @Test
  void even_column_bottom_neighbor() {
    MapPosition neighbor = MapTools.Border.BOTTOM.getNeighbor(0, 5);
    assertEquals(0, neighbor.x);
    assertEquals(4, neighbor.y);
  }

  @Test
  void even_column_top_right_neighbor() {
    // Even column: top right increases x and y
    MapPosition neighbor = MapTools.Border.TOP_RIGHT.getNeighbor(0, 5);
    assertEquals(1, neighbor.x);
    assertEquals(5, neighbor.y); // x%2 == 0, so y + 0
  }

  @Test
  void odd_column_top_right_neighbor() {
    // Odd column: top right increases x and y
    MapPosition neighbor = MapTools.Border.TOP_RIGHT.getNeighbor(1, 5);
    assertEquals(2, neighbor.x);
    assertEquals(6, neighbor.y); // x%2 == 1, so y + 1
  }

  @Test
  void even_column_bottom_right_neighbor() {
    // Even column: bottom right increases x
    MapPosition neighbor = MapTools.Border.BOTTOM_RIGHT.getNeighbor(0, 5);
    assertEquals(1, neighbor.x);
    assertEquals(4, neighbor.y); // (x+1)%2 == 1, so y - 1
  }

  @Test
  void odd_column_bottom_right_neighbor() {
    // Odd column: bottom right increases x
    MapPosition neighbor = MapTools.Border.BOTTOM_RIGHT.getNeighbor(1, 5);
    assertEquals(2, neighbor.x);
    assertEquals(5, neighbor.y); // (x+1)%2 == 0, so y - 0
  }

  @Test
  void all_six_borders_produce_distinct_neighbors() {
    MapPosition pos = new MapPosition(5, 5);
    MapPosition[] neighbors = new MapPosition[6];
    int i = 0;
    for (MapTools.Border border : MapTools.Border.values()) {
      neighbors[i++] = border.getNeighbor(pos);
    }

    // All neighbors should be distinct
    for (int a = 0; a < 6; a++) {
      for (int b = a + 1; b < 6; b++) {
        assertNotEquals(neighbors[a], neighbors[b],
            "Neighbors from different borders should be distinct");
      }
    }
  }

  @Test
  void neighbor_using_position_object() {
    MapPosition pos = new MapPosition(2, 3);
    MapPosition neighbor1 = MapTools.Border.TOP.getNeighbor(pos);
    MapPosition neighbor2 = MapTools.Border.TOP.getNeighbor(pos.x, pos.y);
    assertEquals(neighbor1, neighbor2);
  }
}
