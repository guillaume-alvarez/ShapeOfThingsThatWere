package com.galvarez.ttw.model.components;

import java.util.Collection;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Discovery;

public class Research extends Component {

  public final Collection<String> previous;

  public final Discovery target;

  public int progress = 0;

  public Research(Discovery target) {
    this.previous = target.previous;
    this.target = target;
  }

  @Override
  public String toString() {
    return target.name + "[" + progress + "%]";
  }

}
