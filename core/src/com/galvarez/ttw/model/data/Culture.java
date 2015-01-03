package com.galvarez.ttw.model.data;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.JsonValue;
import com.galvarez.ttw.model.Faction;

// TODO do not allow modification after loading
public final class Culture {

  public final String name;

  public final Array<String> cities;

  public final Map<Faction, Integer> ai;

  public Culture(String name, Array<String> cities, Map<Faction, Integer> ai) {
    this.name = name;
    this.cities = cities;
    this.ai = ai;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  public String newCityName() {
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

  public static final ReadOnlySerializer<Culture> SER = new ReadOnlySerializer<Culture>() {
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
    @Override
    public Culture read(Json json, JsonValue data, Class type) {
      Map<Faction, Integer> ai = new EnumMap<>(Faction.class);
      for (JsonValue child : data.get("ai")) {
        ai.put(Faction.valueOf(child.name), child.asInt());
      }
      return new Culture(data.getString("name"), //
          json.readValue(Array.class, data.get("cities")), //
          ai //
      );
    }
  };
}
