package com.galvarez.ttw.model.components;

import java.util.EnumMap;
import java.util.Map;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Policy;

public final class Policies extends Component {

  public final Map<Policy, Discovery> policies = new EnumMap<>(Policy.class);

  /**
   * Stability percentage, decrease when changing policies, increase with time
   * to stability max value.
   */
  public int stability = 15;

  public int stabilityMax = 15;

  public int stabilityGrowth = 1;

  public Policies() {
  }

}
