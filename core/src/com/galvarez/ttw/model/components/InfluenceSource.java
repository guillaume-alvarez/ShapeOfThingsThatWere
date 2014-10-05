package com.galvarez.ttw.model.components;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.artemis.Component;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;

public final class InfluenceSource extends Component {

  public Empire empire;

  public int power = -1;

  /** Tile to apply the influence to. */
  public MapPosition target;

  public final Set<MapPosition> influencedTiles = new HashSet<>();

  public int powerAdvancement = 0;

  public final Modifiers modifiers = new Modifiers();

  public InfluenceSource(Empire empire) {
    this.empire = empire;
  }

  public static final class Modifiers {

    public final Map<Terrain, Integer> terrainBonus = new EnumMap<Terrain, Integer>(Terrain.class);

    public Modifiers() {
      for (Terrain t : Terrain.values())
        terrainBonus.put(t, Integer.valueOf(0));
    }

  }

  @Override
  public String toString() {
    return empire.toString();
  }

}
