package com.galvarez.ttw.model.components;

import com.artemis.Component;

public final class Score extends Component {

  public int rank;

  public int totalScore = 0;

  public int lastTurnPoints = 0;

  public int nbDiscoveries = 0;

  public int nbDiscoveriesMax = 0;

  public int nbControlled = 0;

  public int nbControlledMax = 0;

  public Score() {
  }

}
