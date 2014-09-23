package com.galvarez.ttw.model.components;

import java.util.List;

import com.artemis.Component;
import com.galvarez.ttw.model.map.MapPosition;

public final class AIControlled extends Component {

  public List<MapPosition> estimatedTiles;

  public int estimationTurn;

  public AIControlled() {
  }

}
