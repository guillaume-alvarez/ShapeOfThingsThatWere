package com.galvarez.ttw.model.data;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.JsonValue;

public final class Building {

  private final String name;

  /**
   * Indexed by effect name, contains the delta for the corresponding variable.
   * Positive numbers are positive modifiers for the discoverer.
   */
  public final Map<String, Object> effects;

  public final Map<String, Object> conditions;

  public final String previous;

  public final String type;

  public Building(String name, String previous, String type, Map<String, Object> effects, Map<String, Object> conditions) {
    this.name = name;
    this.previous = previous;
    this.type = type;
    this.effects = effects;
    this.conditions = conditions;
  }

  public String getName() {
    return name;
  }

  public static final ReadOnlySerializer<Building> SER = new ReadOnlySerializer<Building>() {
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
    @Override
    public Building read(Json json, JsonValue data, Class type) {
      return new Building( //
          data.getString("name"), //
          data.getString("previous", null), //
          data.getString("type", null), //
          json.readValue(HashMap.class, data.get("effects")), //
          json.readValue(HashMap.class, data.get("conditions")) //
      );
    }
  };

}
