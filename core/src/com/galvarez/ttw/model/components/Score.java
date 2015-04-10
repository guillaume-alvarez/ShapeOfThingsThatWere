package com.galvarez.ttw.model.components;

import java.util.HashSet;

import com.artemis.Component;
import com.artemis.Entity;

public final class Score extends Component {

  public int rank;

  public int totalScore = 0;

  public int lastTurnPoints = 0;

  public int nbDiscoveries = 0;

  public int nbDiscoveriesMax = 0;

  public final HashSet<Entity> controlledEmpires = new HashSet<>();

  public int nbControlledMax = 0;

  public Score() {
  }

}
