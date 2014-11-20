package com.galvarez.ttw.model.components;

import java.util.ArrayList;
import java.util.List;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Discovery;

public final class Discoveries extends Component {

  public final List<Discovery> done = new ArrayList<>();

  public Research next;

  public Research last;

  public Discoveries() {
  }

}
