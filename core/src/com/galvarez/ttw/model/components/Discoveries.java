package com.galvarez.ttw.model.components;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Choice;
import com.galvarez.ttw.model.data.Discovery;

public final class Discoveries extends Component {

  public final List<Discovery> discovered = new ArrayList<>();

  public final Map<Choice, Discovery> choices = new EnumMap<>(Choice.class);

  public Research nextDiscovery;

  public Discoveries() {
  }

}
