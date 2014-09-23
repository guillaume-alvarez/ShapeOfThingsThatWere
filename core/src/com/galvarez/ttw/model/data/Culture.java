package com.galvarez.ttw.model.data;

import com.badlogic.gdx.utils.Array;

// TODO do not allow modification after loading
public final class Culture {

  public String name;

  public Array<String> cities;

  public Culture() {
  }

  public Culture(String name, Array<String> cities) {
    this.name = name;
    this.cities = cities;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  String newCityName() {
    return cities.removeIndex(0);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    else if (obj instanceof Culture)
      return name.equals(((Culture) obj).name);
    else
      return false;
  }
}
