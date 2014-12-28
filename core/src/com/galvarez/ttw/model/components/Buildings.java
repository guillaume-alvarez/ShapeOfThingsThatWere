package com.galvarez.ttw.model.components;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Building;

public final class Buildings extends Component {

  public final Map<String, Building> built = new HashMap<>();

  public Building construction;

  public int constructionTurns;

  public Buildings() {
  }

}
