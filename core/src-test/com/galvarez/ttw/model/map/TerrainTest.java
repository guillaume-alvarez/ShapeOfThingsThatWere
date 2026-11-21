package com.galvarez.ttw.model.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for terrain properties.
 */
@DisplayName("Approach A: Pure logic tests - Terrain properties")
class TerrainTest {

  @Test
  void all_terrains_have_positive_move_cost() {
    for (Terrain terrain : Terrain.values()) {
      assertTrue(terrain.moveCost() > 0,
          terrain.name() + " should have positive move cost");
    }
  }

  @Test
  void habitable_terrains_can_start() {
    assertTrue(Terrain.PLAIN.canStart());
    assertTrue(Terrain.GRASSLAND.canStart());
    assertTrue(Terrain.FOREST.canStart());
    assertTrue(Terrain.HILLS.canStart());
  }

  @Test
  void uninhabitable_terrains_cannot_start() {
    assertFalse(Terrain.DEEP_WATER.canStart());
    assertFalse(Terrain.SHALLOW_WATER.canStart());
    assertFalse(Terrain.DESERT.canStart());
    assertFalse(Terrain.MOUNTAIN.canStart());
    assertFalse(Terrain.ARCTIC.canStart());
  }

  @Test
  void difficult_terrains_have_higher_move_cost() {
    // Mountains and deep water should be harder to cross than plains
    assertTrue(Terrain.MOUNTAIN.moveCost() > Terrain.PLAIN.moveCost());
    assertTrue(Terrain.DEEP_WATER.moveCost() > Terrain.PLAIN.moveCost());
    assertTrue(Terrain.ARCTIC.moveCost() > Terrain.PLAIN.moveCost());
  }

  @Test
  void plains_have_lowest_move_cost_among_habitable() {
    int minCost = Integer.MAX_VALUE;
    for (Terrain terrain : Terrain.values()) {
      if (terrain.canStart()) {
        minCost = Math.min(minCost, terrain.moveCost());
      }
    }
    assertEquals(Terrain.PLAIN.moveCost(), minCost);
  }

  @Test
  void terrain_texture_matches_ordinal() {
    for (Terrain terrain : Terrain.values()) {
      assertEquals(terrain.ordinal(), terrain.getTexture());
    }
  }

  @Test
  void terrain_move_block_is_false() {
    // Currently no terrain blocks movement
    for (Terrain terrain : Terrain.values()) {
      assertFalse(terrain.moveBlock());
    }
  }
}
