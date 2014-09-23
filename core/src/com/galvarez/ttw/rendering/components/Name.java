package com.galvarez.ttw.rendering.components;

import com.artemis.Component;

public final class Name extends Component {

  public String name;

  public Name(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

}
