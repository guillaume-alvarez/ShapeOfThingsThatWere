package com.galvarez.ttw.model.components;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.artemis.Component;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;

/**
 * Store destination (and the path to it) for an entity.
 * 
 * @author Guillaume Alvarez
 */
public final class Destination extends Component {

  public Set<Terrain> forbiddenTiles = EnumSet.of(Terrain.SHALLOW_WATER, Terrain.DEEP_WATER, Terrain.ARCTIC);

  public MapPosition target;

  public List<MapPosition> path;

  /** Turns spent moving to next tile. */
  public int progress;

  public Destination() {
  }

}
