package com.galvarez.ttw.model;

import static java.lang.Math.max;

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

  private interface Effect<V> {

    void apply(V value, Entity empire, boolean revert);

    void addFactionsScores(ObjectFloatMap<Faction> scores, V value);

    String toString(V value);

  }

  private final Map<String, Effect<?>> effects = new HashMap<>();
  {
    effects.put("stability", new Effect<Number>() {
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
        scores.getAndIncrement(Faction.MILITARY, 0, delta / 2);
        scores.getAndIncrement(Faction.ECONOMIC, 0, delta);
        scores.getAndIncrement(Faction.CULTURAL, 0, delta);
      }

      @Override
      public String toString(Number value) {
        return "stability: " + value;
      }
    });
    effects.put("discovery", new Effect<Number>() {
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
        return "discovery: " + value;
      }
    });
    effects.put("growth", new Effect<Number>() {
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
        return "growth: " + value;
      }
    });
    effects.put("diplomacy", new Effect<String>() {
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
    });
    effects.put("militaryPower", new Effect<Number>() {
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
        scores.getAndIncrement(Faction.MILITARY, 0, delta * 2);
        scores.getAndIncrement(Faction.ECONOMIC, 0, delta / 2);
        scores.getAndIncrement(Faction.CULTURAL, 0, max(0, -delta));
      }

      @Override
      public String toString(Number value) {
        return "militaryPower: " + value;
      }
    });

    for (Terrain t : Terrain.values())
      effects.put(t.name(), new Effect<Number>() {
        @Override
        public void apply(Number value, Entity empire, boolean revert) {
          InfluenceSource source = getInfluence(empire);
          int delta = value.intValue();
          Integer current = source.modifiers.terrainBonus.get(t);
          if (revert)
            source.modifiers.terrainBonus.put(t, current == null ? 0 : current - delta);
          else
            source.modifiers.terrainBonus.put(t, current == null ? delta : current + delta);
        }

        @Override
        public void addFactionsScores(ObjectFloatMap<Faction> scores, Number value) {
          float delta = value.intValue();
          scores.getAndIncrement(Faction.MILITARY, 0, max(0f, delta));
          scores.getAndIncrement(Faction.ECONOMIC, 0, max(0f, delta / 2));
        }

        @Override
        public String toString(Number value) {
          return t.getDesc() + ": " + value;
        }
      });
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
