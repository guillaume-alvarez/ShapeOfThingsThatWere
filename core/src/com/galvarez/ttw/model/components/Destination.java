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

  public final Set<Terrain> forbiddenTiles;

  public MapPosition target;

  public List<MapPosition> path;

  /** Turns spent moving to next tile. */
  public int progress;

  public Destination(Set<Terrain> forbiddenTiles) {
    this.forbiddenTiles = EnumSet.copyOf(forbiddenTiles);
  }
}
