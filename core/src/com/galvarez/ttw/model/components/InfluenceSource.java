package com.galvarez.ttw.model.components;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.artemis.Component;
import com.artemis.Entity;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;

public final class InfluenceSource extends Component {

  private float power = InfluenceSystem.INITIAL_POWER;

  /** In per mille. */
  public int growth;

  /** Usually compared to power. */
  public int health = InfluenceSystem.INITIAL_POWER;

  public final Set<MapPosition> influencedTiles = new HashSet<>();

  public final List<Entity> secondarySources = new ArrayList<>();

  public final Modifiers modifiers = new Modifiers();

  public static final class Modifiers {

    public final Map<Terrain, Integer> terrainBonus = new EnumMap<Terrain, Integer>(Terrain.class);

    public Modifiers() {
      for (Terrain t : Terrain.values())
        terrainBonus.put(t, Integer.valueOf(0));
    }

  }

  public int power() {
    return (int) power;
  }

  public void addToPower(float f) {
    power += f;
  }

  public void setPower(int power) {
    this.power = power;
  }

}
