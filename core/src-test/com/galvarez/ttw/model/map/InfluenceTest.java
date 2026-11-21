package com.galvarez.ttw.model.map;

import com.artemis.Entity;
import com.artemis.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Influence data class.
 * Uses a minimal Artemis World only for entity creation,
 * but tests the Influence logic directly without GameMap.
 */
@DisplayName("Approach A: Pure logic tests - Influence data class")
class InfluenceTest {

  private World world;

  @BeforeEach
  void setUp() {
    world = new World();
    world.initialize();
  }

  @Test
  void empty_tile_has_no_main_influence() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    assertFalse(inf.hasMainInfluence());
    assertEquals(-1, inf.getMainInfluenceSource());
  }

  @Test
  void setting_influence_claims_tile() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 100);

    assertTrue(inf.hasMainInfluence());
    assertEquals(entity.getId(), inf.getMainInfluenceSource());
    assertEquals(100, inf.getInfluence(entity));
  }

  @Test
  void higher_influence_wins_competition() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity1 = world.createEntity();
    Entity entity2 = world.createEntity();

    inf.setInfluence(entity1, 80);
    inf.setInfluence(entity2, 100);

    assertEquals(entity2.getId(), inf.getMainInfluenceSource());
    assertEquals(20, inf.getSecondInfluenceDiff());
  }

  @Test
  void influence_below_terrain_cost_does_not_claim() {
    // PLAIN has moveCost 10, threshold is 9
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 5);

    assertFalse(inf.hasMainInfluence());
  }

  @Test
  void influence_equal_to_terrain_cost_does_not_claim() {
    // Threshold is moveCost - 1 = 9, so 9 does not claim
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 9);

    assertFalse(inf.hasMainInfluence());
  }

  @Test
  void influence_above_terrain_cost_claims() {
    // Threshold is moveCost - 1 = 9, so 10 claims
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 10);

    assertTrue(inf.hasMainInfluence());
  }

  @Test
  void influence_increases_toward_target() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 50);
    inf.increaseTarget(entity, 100);

    inf.computeNewInfluence();

    int newInfluence = inf.getInfluence(entity);
    assertTrue(newInfluence > 50, "Influence should increase toward target");
    assertTrue(inf.getDelta(entity) > 0, "Delta should be positive");
  }

  @Test
  void influence_decreases_toward_lower_target() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 100);
    inf.increaseTarget(entity, 0);

    inf.computeNewInfluence();

    int newInfluence = inf.getInfluence(entity);
    assertTrue(newInfluence < 100, "Influence should decrease toward lower target");
    assertTrue(inf.getDelta(entity) < 0, "Delta should be negative");
  }

  @Test
  void influence_delta_is_at_least_one() {
    // The algorithm ensures delta is at least 1
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 50);
    inf.increaseTarget(entity, 51);

    inf.computeNewInfluence();

    assertTrue(Math.abs(inf.getDelta(entity)) >= 1,
        "Delta should be at least 1 in absolute value");
  }

  @Test
  void removing_influence_clears_tile() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 100);
    assertTrue(inf.hasMainInfluence());

    inf.removeInfluence(entity);
    assertFalse(inf.hasMainInfluence());
    assertEquals(0, inf.getInfluence(entity));
  }

  @Test
  void second_influence_diff_is_correct() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity1 = world.createEntity();
    Entity entity2 = world.createEntity();

    inf.setInfluence(entity1, 100);
    inf.setInfluence(entity2, 60);

    assertEquals(40, inf.getSecondInfluenceDiff());
  }

  @Test
  void required_influence_for_main_influencer_is_zero() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity = world.createEntity();
    inf.setInfluence(entity, 100);

    assertEquals(0, inf.requiredInfluence(entity));
  }

  @Test
  void required_influence_for_non_influencer() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity1 = world.createEntity();
    Entity entity2 = world.createEntity();

    inf.setInfluence(entity1, 100);
    inf.setInfluence(entity2, 50);

    // entity2 needs to exceed mainInfluence (100) and terrain cost
    // required = max(terrain.moveCost, mainInfluence + 1) - current
    // = max(10, 101) - 50 = 51
    assertEquals(51, inf.requiredInfluence(entity2));
  }

  @Test
  void influence_position_equality() {
    MapPosition pos = new MapPosition(3, 4);
    Influence inf1 = new Influence(pos, Terrain.PLAIN);
    Influence inf2 = new Influence(pos, Terrain.FOREST);

    assertEquals(inf1, inf2, "Influence equality is based on position");
  }

  @Test
  void max_influence_considers_terrain_cost() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    // No entities, max should be at least terrain cost
    assertTrue(inf.getMaxInfluence() >= Terrain.PLAIN.moveCost());
  }

  @Test
  void total_influence_sums_all_sources() {
    Influence inf = new Influence(new MapPosition(0, 0), Terrain.PLAIN);
    Entity entity1 = world.createEntity();
    Entity entity2 = world.createEntity();

    inf.setInfluence(entity1, 50);
    inf.setInfluence(entity2, 30);

    assertEquals(80, inf.getTotalInfluence());
  }
}
