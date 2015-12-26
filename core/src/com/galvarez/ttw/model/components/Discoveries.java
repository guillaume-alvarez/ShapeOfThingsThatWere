package com.galvarez.ttw.model.components;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.artemis.Component;
import com.galvarez.ttw.model.Faction;
import com.galvarez.ttw.model.data.Discovery;

public final class Discoveries extends Component {

  public final Set<Discovery> done = new HashSet<>();

  /** Increase to speed progress up. */
  public int progressPerTurn = 1;

  /** Contains possibles discoveries when one is expected. */
  public Map<Faction, Research> nextPossible;

  public Research last;

  public Discoveries() {
  }

}
