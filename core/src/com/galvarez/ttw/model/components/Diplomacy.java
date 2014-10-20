package com.galvarez.ttw.model.components;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.artemis.Component;
import com.artemis.Entity;
import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.DiplomaticSystem.State;

public final class Diplomacy extends Component {

  public final Map<Entity, State> relations = new HashMap<>();

  public final Map<Entity, Integer> lastChange = new HashMap<>();

  public final Map<Entity, Action> proposals = new HashMap<>();

  public final EnumSet<State> knownStates = EnumSet.of(State.NONE);

  public Diplomacy() {
  }

  public State getRelationWith(Entity other) {
    State state = relations.get(other);
    return state != null ? state : State.NONE;
  }

}
