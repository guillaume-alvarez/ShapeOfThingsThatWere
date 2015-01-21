package com.galvarez.ttw.model.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.artemis.Component;
import com.artemis.Entity;
import com.galvarez.ttw.model.map.MapPosition;

public final class AIControlled extends Component {

  /** All neighboring empires. */
  public final Set<Entity> neighbors = new HashSet<>();

  public List<MapPosition> estimatedTiles;

  public int estimationTurn;

  public int lastMove;

  public MapPosition lastPosition;

  public AIControlled() {
  }

}
