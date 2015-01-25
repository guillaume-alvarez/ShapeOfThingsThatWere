package com.galvarez.ttw.model.components;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.artemis.Component;
import com.artemis.Entity;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;

public final class InfluenceSource extends Component {

  public Entity empire;

  public int power = -1;

  public final Set<MapPosition> influencedTiles = new HashSet<>();

  public int growth;

  public int powerAdvancement = 0;

  public final List<Entity> secondarySources = new ArrayList<>();

  public final Modifiers modifiers = new Modifiers();

  public static final class Modifiers {

    public final Map<Terrain, Integer> terrainBonus = new EnumMap<Terrain, Integer>(Terrain.class);

    public Modifiers() {
      for (Terrain t : Terrain.values())
        terrainBonus.put(t, Integer.valueOf(0));
    }

  }

  @Override
  public String toString() {
    return empire.getComponent(Empire.class).toString();
  }

}
