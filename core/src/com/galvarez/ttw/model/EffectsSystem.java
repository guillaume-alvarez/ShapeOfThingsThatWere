package com.galvarez.ttw.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.map.Terrain;

@Wire
public final class EffectsSystem extends VoidEntitySystem {

  private ComponentMapper<Policies> policies;

  private ComponentMapper<Army> armies;

  private ComponentMapper<Discoveries> discoveries;

  private ComponentMapper<Diplomacy> diplomacies;

  private ComponentMapper<Capital> capitals;

  private ComponentMapper<InfluenceSource> influences;

  private final class TerrainEffect implements Effect<Number> {
    private final Terrain terrain;

    public TerrainEffect(Terrain t) {
      this.terrain = t;
    }

    @Override
    public void apply(Number value, Entity empire, boolean revert) {
      InfluenceSource source = getInfluence(empire);
      int delta = value.intValue();
      Integer current = source.modifiers.terrainBonus.get(terrain);
      if (revert)
        source.modifiers.terrainBonus.put(terrain, current == null ? 0 : current - delta);
      else
        source.modifiers.terrainBonus.put(terrain, current == null ? delta : current + delta);
    }

    @Override
    public void addFactionsScores(ObjectFloatMap<Faction> scores, Number value) {
      float delta = value.intValue();
      scores.getAndIncrement(Faction.MILITARY, 0, delta / 2f);
      scores.getAndIncrement(Faction.ECONOMIC, 0, delta);
    }

    @Override
    public String toString(Number value) {
      int i = value.intValue();
      return terrain.getDesc() + (i > 0 ? ": +" : " ") + i;
    }
  }

  private final class StabilityEffect implements Effect<Number> {
    @Override
    public void apply(Number value, Entity empire, boolean revert) {
      Policies p = policies.get(empire);
      if (revert)
        p.stabilityGrowth -= value.intValue();
      else
        p.stabilityGrowth += value.intValue();
    }

    @Override
    public void addFactionsScores(ObjectFloatMap<Faction> scores, Number value) {
      float delta = value.intValue();
      scores.getAndIncrement(Faction.ECONOMIC, 0, delta / 2f);
      scores.getAndIncrement(Faction.CULTURAL, 0, delta);
    }

    @Override
    public String toString(Number value) {
      int i = value.intValue();
      return (i > 0 ? "stability: +" : "stability: ") + i;
    }
  }

  private final class MilitaryEffect implements Effect<Number> {
    @Override
    public void apply(Number value, Entity empire, boolean revert) {
      Army army = armies.get(empire);
      if (revert)
        army.militaryPower -= value.intValue();
      else
        army.militaryPower += value.intValue();
    }

    @Override
    public void addFactionsScores(ObjectFloatMap<Faction> scores, Number value) {
      float delta = value.intValue();
      scores.getAndIncrement(Faction.MILITARY, 0, delta * 2f);
      scores.getAndIncrement(Faction.CULTURAL, 0, -delta);
    }

    @Override
    public String toString(Number value) {
      int i = value.intValue();
      return (i > 0 ? "military power: +" : "military power: ") + i;
    }
  }

  private final class DiplomacyEffect implements Effect<String> {
    @Override
    public void apply(String value, Entity empire, boolean revert) {
      // cannot revert that one
      Diplomacy diplomacy = diplomacies.get(empire);
      diplomacy.knownStates.add(State.valueOf(value));
    }

    @Override
    public void addFactionsScores(ObjectFloatMap<Faction> scores, String value) {
      scores.getAndIncrement(Faction.MILITARY, 0, 3f);
      scores.getAndIncrement(Faction.ECONOMIC, 0, 1f);
      scores.getAndIncrement(Faction.CULTURAL, 0, 0.5f);
    }

    @Override
    public String toString(String value) {
      return "diplomacy: " + value;
    }
  }

  private final class DiscoveryEffect implements Effect<Number> {
    @Override
    public void apply(Number value, Entity empire, boolean revert) {
      Discoveries d = discoveries.get(empire);
      if (revert)
        d.progressPerTurn -= value.intValue();
      else
        d.progressPerTurn += value.intValue();
    }

    @Override
    public void addFactionsScores(ObjectFloatMap<Faction> scores, Number value) {
      float delta = value.intValue();
      scores.getAndIncrement(Faction.CULTURAL, 0, delta * 2);
    }

    @Override
    public String toString(Number value) {
      int i = value.intValue();
      return (i > 0 ? "discovery: +" : "discovery: ") + i;
    }
  }

  private final class GrowthEffect implements Effect<Number> {
    @Override
    public void apply(Number value, Entity empire, boolean revert) {
      InfluenceSource source = getInfluence(empire);
      if (revert)
        source.growth -= value.intValue();
      else
        source.growth += value.intValue();
    }

    @Override
    public void addFactionsScores(ObjectFloatMap<Faction> scores, Number value) {
      float delta = value.intValue();
      scores.getAndIncrement(Faction.ECONOMIC, 0, delta * 2);
      scores.getAndIncrement(Faction.CULTURAL, 0, delta / 2);
    }

    @Override
    public String toString(Number value) {
      int i = value.intValue();
      return (i > 0 ? "growth power: +" : "growth power: ") + i;
    }
  }

  private interface Effect<V> {

    void apply(V value, Entity empire, boolean revert);

    void addFactionsScores(ObjectFloatMap<Faction> scores, V value);

    String toString(V value);

  }

  private final Map<String, Effect<?>> effects = new HashMap<>();
  {
    effects.put("stability", new StabilityEffect());
    effects.put("discovery", new DiscoveryEffect());
    effects.put("growth", new GrowthEffect());
    effects.put("diplomacy", new DiplomacyEffect());
    effects.put("militaryPower", new MilitaryEffect());

    for (Terrain t : Terrain.values())
      effects.put(t.name(), new TerrainEffect(t));
  }

  public EffectsSystem() {
  }

  private InfluenceSource getInfluence(Entity empire) {
    Entity capital = capitals.get(empire).capital;
    InfluenceSource influence = influences.get(capital);
    return influence;
  }

  @Override
  protected void processSystem() {
    // nothing to do, called by other systems to add and remove effects
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void apply(Map<String, Object> effectsMap, Entity empire, boolean revert) {
    for (Entry<String, Object> e : effectsMap.entrySet()) {
      Effect effect = effects.get(e.getKey());
      effect.apply(e.getValue(), empire, revert);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<String> toString(Map<String, Object> effectsMap) {
    List<String> list = new ArrayList<>();
    for (Entry<String, Object> e : effectsMap.entrySet()) {
      Effect effect = effects.get(e.getKey());
      list.add(effect.toString(e.getValue()));
    }
    return list;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public ObjectFloatMap<Faction> getFactionsScores(Map<String, Object> effectsMap) {
    ObjectFloatMap<Faction> scores = new ObjectFloatMap<>(Faction.values().length);
    for (Entry<String, Object> effect : effectsMap.entrySet()) {
      Effect e = effects.get(effect.getKey());
      e.addFactionsScores(scores, effect.getValue());
    }
    return scores;
  }
}
