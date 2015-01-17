package com.galvarez.ttw.model.components;

import java.util.List;

import com.artemis.Component;
import com.galvarez.ttw.model.map.MapPosition;

/**
 * Store destination (and the path to it) for an entity.
 * 
 * @author Guillaume Alvarez
 */
public final class Destination extends Component {

  public MapPosition target;

  public List<MapPosition> path;

  public Destination() {
  }

}
