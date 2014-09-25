package com.galvarez.ttw.model.components;

import com.artemis.Component;
import com.artemis.Entity;

public final class Capital extends Component {

  public final Entity capital;

  public Capital(Entity capital) {
    this.capital = capital;
  }

}
