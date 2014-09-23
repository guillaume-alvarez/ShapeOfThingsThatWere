package com.galvarez.ttw.model.components;

import java.util.HashSet;
import java.util.Set;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;

public final class InfluenceSource extends Component {

  public Empire empire;

  public int power = -1;

  /** Tile to apply the influence to. */
  public MapPosition target;

  public final Set<MapPosition> influencedTiles = new HashSet<>();

  public int powerAdvancement = 0;

  public InfluenceSource(Empire empire) {
    this.empire = empire;
  }

  @Override
  public String toString() {
    return empire.toString();
  }

}
