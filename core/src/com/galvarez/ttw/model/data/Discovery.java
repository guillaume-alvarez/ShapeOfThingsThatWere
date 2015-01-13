package com.galvarez.ttw.model.data;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.galvarez.ttw.model.Faction;
import com.galvarez.ttw.model.map.Terrain;

public final class Discovery {

  public final String name;

  public final Set<Terrain> terrains = EnumSet.noneOf(Terrain.class);

  public final Set<String> groups;

  public final Set<Set<String>> previous;

  public ObjectFloatMap<Faction> factions;

  /**
   * Indexed by effect name, contains the delta for the corresponding variable.
   * Positive numbers are positive modifiers for the discoverer.
   */
  public final Map<String, Object> effects;

  private Discovery(String name, List<List<String>> previous, List<String> groups, List<Terrain> terrains,
      Map<String, Object> effects) {
    this.name = name;
    this.effects = effects != null ? effects : Collections.emptyMap();
    this.previous = previous.stream().map(l -> set(l)).collect(toSet());
    this.groups = set(groups);
    if (terrains != null)
      this.terrains.addAll(terrains);
  }

  @SuppressWarnings("unchecked")
  private static <T> Set<T> set(List<T> list) {
    if (list == null || list.isEmpty())
      return Collections.emptySet();
    if (list.size() == 1)
      return Collections.singleton(list.get(0));
    else if (list instanceof Set)
      return (Set<T>) list;
    else
      return new HashSet<>(list);
  }

  public Discovery(String name) {
    this(name, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
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

  public static final ReadOnlySerializer<Discovery> SER = new ReadOnlySerializer<Discovery>() {
    @SuppressWarnings("unchecked")
    @Override
    public Discovery read(Json json, JsonValue data, Class type) {
      return new Discovery(data.getString("name"), //
          json.readValue(ArrayList.class, ArrayList.class, data.get("previous")), //
          json.readValue(ArrayList.class, data.get("groups")), //
          json.readValue(ArrayList.class, Terrain.class, data.get("terrains")), //
          json.readValue(HashMap.class, data.get("effects")));
    }
  };

}
