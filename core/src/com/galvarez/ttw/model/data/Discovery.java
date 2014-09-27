package com.galvarez.ttw.model.data;

import java.util.Collections;
import java.util.List;

import com.galvarez.ttw.model.map.Terrain;

// TODO do not allow modification after loading
public final class Discovery {

  public String name;

  public List<Terrain> terrains;

  public List<String> groups = Collections.emptyList();

  public List<String> previous = Collections.emptyList();

  public Discovery() {
  }

  public Discovery(String name, List<String> previous) {
    this.name = name;
    this.previous = previous;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    else if (obj instanceof Discovery)
      return name.equals(((Discovery) obj).name);
    else
      return false;
  }
}
