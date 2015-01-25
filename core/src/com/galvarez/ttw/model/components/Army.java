package com.galvarez.ttw.model.components;

import com.artemis.Component;
import com.artemis.Entity;

/**
 * Represents an individual army.
 * 
 * @author Guillaume Alvarez
 */
public final class Army extends Component {

  public final int maxPower;

  public int currentPower;

  public final Entity source;

  public Army(Entity source, int militaryPower) {
    this.source = source;
    this.maxPower = militaryPower;
    this.currentPower = militaryPower;
  }

}
