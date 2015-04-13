package com.galvarez.ttw.model.components;

import java.util.ArrayList;
import java.util.List;

import com.artemis.Component;
import com.galvarez.ttw.model.map.MapPosition;

public final class AIControlled extends Component {

  public int lastMove;

  public MapPosition lastPosition;

  public int armiesTargetsComputationTurn = Integer.MIN_VALUE;

  public final List<MapPosition> armiesTargets = new ArrayList<>();

  public AIControlled() {
  }

}
