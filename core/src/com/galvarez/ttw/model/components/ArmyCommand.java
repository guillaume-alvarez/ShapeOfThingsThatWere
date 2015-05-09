package com.galvarez.ttw.model.components;

import java.util.EnumSet;
import java.util.Set;

import com.artemis.Component;
import com.galvarez.ttw.model.map.Terrain;

public final class ArmyCommand extends Component {

  public Set<Terrain> forbiddenTiles = EnumSet.of(Terrain.SHALLOW_WATER, Terrain.DEEP_WATER, Terrain.ARCTIC);

  public int militaryPower = 0;

  public int usedPower = 0;

  public int counter = 0;

}
