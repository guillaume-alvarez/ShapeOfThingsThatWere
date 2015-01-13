package com.galvarez.ttw.model.components;

import java.util.Collection;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Discovery;

public class Research extends Component {

  public final Collection<Discovery> previous;

  public final Discovery target;

  public int progress = 0;

  public Research(Discovery target, Collection<Discovery> previous) {
    this.target = target;
    this.previous = previous;
  }

  @Override
  public String toString() {
    return target.name + "[" + progress + "%]";
  }

}
