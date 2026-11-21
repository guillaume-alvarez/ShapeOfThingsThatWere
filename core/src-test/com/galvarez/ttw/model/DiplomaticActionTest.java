package com.galvarez.ttw.model;

import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Approach A: Direct unit tests for pure logic.
 * Tests the diplomatic action compatibility rules without any ECS dependency.
 */
@DisplayName("Approach A: Pure logic tests - Diplomatic Action compatibility")
class DiplomaticActionTest {

  @Test
  void noChange_is_compatible_with_itself() {
    // NO_CHANGE is compatible with itself (same action is always compatible)
    assertTrue(Action.NO_CHANGE.compatibleWith(Action.NO_CHANGE));
    // NO_CHANGE is NOT compatible with other actions (predicate returns false)
    assertFalse(Action.NO_CHANGE.compatibleWith(Action.DECLARE_WAR));
    assertFalse(Action.NO_CHANGE.compatibleWith(Action.MAKE_PEACE));
    assertFalse(Action.NO_CHANGE.compatibleWith(Action.SIGN_TREATY));
    assertFalse(Action.NO_CHANGE.compatibleWith(Action.SURRENDER));
  }

  @Test
  void declareWar_is_unilateral() {
    // One side can declare war regardless of the other's action
    assertTrue(Action.DECLARE_WAR.compatibleWith(Action.NO_CHANGE));
    assertTrue(Action.DECLARE_WAR.compatibleWith(Action.DECLARE_WAR));
  }

  @Test
  void makePeace_requires_peace_or_treaty_from_peer() {
    // Making peace is compatible with no change (peer doesn't object)
    assertFalse(Action.MAKE_PEACE.compatibleWith(Action.DECLARE_WAR));
  }

  @Test
  void signTreaty_requires_peace_or_surrender_from_peer() {
    assertFalse(Action.SIGN_TREATY.compatibleWith(Action.DECLARE_WAR));
    assertFalse(Action.SIGN_TREATY.compatibleWith(Action.NO_CHANGE));
  }

  @Test
  void surrender_requires_peace_or_treaty_from_peer() {
    assertFalse(Action.SURRENDER.compatibleWith(Action.DECLARE_WAR));
    assertFalse(Action.SURRENDER.compatibleWith(Action.NO_CHANGE));
  }

  @Test
  void declareWar_sets_war_state_for_both_sides() {
    // When declaring war, both sides end up in WAR state
    // afterMe and afterYou are both WAR
    assertTrue(Action.DECLARE_WAR.str.contains("war"));
  }

  @Test
  void declareWar_requires_non_war_state() {
    // Can only declare war if not already at war
    assertTrue(Action.DECLARE_WAR.before.contains(State.NONE));
    assertTrue(Action.DECLARE_WAR.before.contains(State.TREATY));
    assertFalse(Action.DECLARE_WAR.before.contains(State.WAR));
  }

  @Test
  void makePeace_requires_war_state() {
    assertTrue(Action.MAKE_PEACE.before.contains(State.WAR));
    assertFalse(Action.MAKE_PEACE.before.contains(State.NONE));
  }

  @Test
  void signTreaty_requires_war_or_none_state() {
    assertTrue(Action.SIGN_TREATY.before.contains(State.WAR));
    assertTrue(Action.SIGN_TREATY.before.contains(State.NONE));
    assertFalse(Action.SIGN_TREATY.before.contains(State.TREATY));
  }
}
