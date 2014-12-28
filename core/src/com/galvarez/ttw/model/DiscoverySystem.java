package com.galvarez.ttw.model;

import static java.lang.Math.max;
import static java.lang.String.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Building;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Policy;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.NotificationsSystem.Condition;
import com.galvarez.ttw.rendering.NotificationsSystem.Type;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * For every empire, compute the new discovery.
 * <p>
 * Every turn a certain progress is made toward discovery is made, depending on
 * its capital power and influence.
 * </p>
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class DiscoverySystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(DiscoverySystem.class);

  /** This value permits to display values as percentages. */
  private static final int DISCOVERY_THRESHOLD = 100;

  /** Increase to speed progress up. */
  private static final int PROGRESS_PER_TILE = 1;

  private interface Effect<V> {

    void apply(V value, DiscoverySystem system, Entity entity, boolean revert);

    void addFactionsScores(ObjectFloatMap<Faction> scores, V value);

    String toString(V value);

  }

  private static final Map<String, Effect<? extends Object>> EFFECTS = new HashMap<>();
  static {
    EFFECTS.put("stability", new Effect<Number>() {
      @Override
      public void apply(Number value, DiscoverySystem system, Entity entity, boolean revert) {
        Policies empire = system.policies.get(entity);
        if (revert)
          empire.stabilityGrowth -= value.intValue();
        else
          empire.stabilityGrowth += value.intValue();
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
    EFFECTS.put("discovery", new Effect<Number>() {
      @Override
      public void apply(Number value, DiscoverySystem system, Entity entity, boolean revert) {
        Discoveries empire = system.empires.get(entity);
        if (revert)
          empire.progressPerTurn -= value.intValue();
        else
          empire.progressPerTurn += value.intValue();
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
    EFFECTS.put("growth", new Effect<Number>() {
      @Override
      public void apply(Number value, DiscoverySystem system, Entity entity, boolean revert) {
        InfluenceSource source = system.getInfluence(entity);
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
    EFFECTS.put("diplomacy", new Effect<String>() {
      @Override
      public void apply(String value, DiscoverySystem system, Entity entity, boolean revert) {
        // cannot revert that one
        Diplomacy diplomacy = entity.getComponent(Diplomacy.class);
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
    EFFECTS.put("militaryPower", new Effect<Number>() {
      @Override
      public void apply(Number value, DiscoverySystem system, Entity entity, boolean revert) {
        Army army = system.armies.get(entity);
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
      EFFECTS.put(t.name(), new Effect<Number>() {
        @Override
        public void apply(Number value, DiscoverySystem system, Entity entity, boolean revert) {
          InfluenceSource source = system.getInfluence(entity);
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

  private final OverworldScreen screen;

  private final Map<String, Discovery> discoveries;

  private final Map<String, Building> buildings;

  private final GameMap map;

  private final Random rand = new Random();

  private NotificationsSystem notifications;

  private ComponentMapper<Discoveries> empires;

  private ComponentMapper<Policies> policies;

  private ComponentMapper<Army> armies;

  private ComponentMapper<Capital> capitals;

  private ComponentMapper<InfluenceSource> influences;

  private ComponentMapper<AIControlled> ai;

  @SuppressWarnings("unchecked")
  public DiscoverySystem(SessionSettings s, GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(Discoveries.class, Capital.class));
    this.discoveries = s.getDiscoveries();
    this.buildings = s.getBuildings();
    this.map = map;
    this.screen = screen;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    if (e == screen.player) {
      Discoveries d = empires.get(e);
      notifications.addNotification(() -> screen.discoveryMenu(), () -> d.next != null, Type.DISCOVERY,
          "No research selected!");
    }
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity entity : entities) {
      Discoveries discovery = empires.get(entity);
      if (discovery.next != null) {
        if (progressNext(discovery, getInfluence(entity)))
          discoverNext(entity, discovery);
      }
    }
  }

  private boolean progressNext(Discoveries discovery, InfluenceSource influence) {
    int progress = discovery.progressPerTurn;
    Set<Terrain> terrains = discovery.next.target.terrains;
    if (terrains != null && !terrains.isEmpty()) {
      for (MapPosition pos : influence.influencedTiles) {
        if (terrains.contains(map.getTerrainAt(pos)))
          progress += PROGRESS_PER_TILE;
      }
    }
    discovery.next.progress += progress;
    return discovery.next.progress >= DISCOVERY_THRESHOLD;
  }

  private InfluenceSource getInfluence(Entity empire) {
    Entity capital = capitals.get(empire).capital;
    InfluenceSource influence = influences.get(capital);
    return influence;
  }

  private void discoverNext(Entity entity, Discoveries discovery) {
    Research next = discovery.next;
    Discovery target = next.target;
    log.info("{} discovered {} from {}.", entity.getComponent(Name.class), target, next.previous);
    if (!ai.has(entity)) {
      Condition condition = !possibleDiscoveries(entity, discovery).isEmpty() ? (() -> discovery.next != null) : null;
      notifications.addNotification(() -> screen.discoveryMenu(), condition, Type.DISCOVERY, "Discovered %s: %s",
          target, target.effects.isEmpty() ? "No effect." : join(", ", effectsStrings(target)));
    }
    discovery.done.add(target);
    discovery.next = null;

    // remove last discovery 2x bonus (to keep only the basic effect)
    if (discovery.last != null)
      applyDiscoveryEffects(discovery.last.target.effects, entity, true);
    discovery.last = next;

    // apply new discovery
    applyDiscoveryEffects(target.effects, entity, false);
    // ... and bonus time until next discovery!
    applyDiscoveryEffects(target.effects, entity, false);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  void applyDiscoveryEffects(Map<String, Object> effects, Entity empire, boolean revert) {
    for (Entry<String, Object> effect : effects.entrySet()) {
      String name = effect.getKey();
      Effect e = EFFECTS.get(name);
      e.apply(effect.getValue(), this, empire, revert);
    }
  }

  /**
   * Compute the possible discoveries for the empire, associated to the faction
   * that recommends them.
   */
  public Map<Faction, Research> possibleDiscoveries(Entity entity, Discoveries empire) {
    // collect current state
    Set<String> done = new HashSet<>();
    Map<String, List<String>> groups = new HashMap<>();
    for (Discovery d : empire.done) {
      done.add(d.name);
      done.addAll(d.groups);
      for (String g : d.groups) {
        List<String> list = groups.get(g);
        if (list == null)
          groups.put(g, list = new ArrayList<>());
        list.add(d.name);
      }
    }

    // select new possible discoveries
    Predicate<Discovery> cannotBeDiscovered = d -> done.contains(d.name) || !done.containsAll(d.previous)
        || !hasTerrain(entity, d.terrains);
    List<Discovery> possible = new ArrayList<>(discoveries.values());
    possible.removeIf(cannotBeDiscovered);
    Collections.shuffle(possible, rand);

    Map<Faction, Research> res = new EnumMap<>(Faction.class);
    // shuffle factions list so that result changes every time
    List<Faction> factions = Arrays.asList(Faction.values());
    Collections.shuffle(factions, rand);
    for (Faction f : factions) {
      float max = Float.NEGATIVE_INFINITY;
      Discovery chosen = null;
      for (Discovery d : possible) {
        float score = d.factions.get(f, 0);
        for (String p : d.previous) {
          Discovery previous = discoveries.get(p);
          if (previous != null)
            score += previous.factions.get(f, 0);
        }
        if (score > max) {
          chosen = d;
          max = score;
        }
      }
      if (chosen != null) {
        possible.remove(chosen);
        List<String> previous = new ArrayList<>(chosen.previous.size());
        chosen.previous.forEach(p -> {
          List<String> group = groups.get(p);
          previous.add(group == null ? p : group.get(rand.nextInt(group.size())));
        });
        res.put(f, new Research(chosen, previous));
      }
    }
    return res;
  }

  private boolean hasTerrain(Entity entity, Set<Terrain> terrains) {
    if (terrains == null || terrains.isEmpty())
      return true;

    InfluenceSource influence = getInfluence(entity);
    for (MapPosition pos : influence.influencedTiles)
      if (terrains.contains(map.getTerrainAt(pos)))
        return true;

    return false;
  }

  public int guessNbTurns(Discoveries discovery, Entity empire, Discovery d) {
    InfluenceSource influence = getInfluence(empire);

    Set<Terrain> terrains = d.terrains;
    int progressPerTurn = discovery.progressPerTurn;
    if (terrains != null && !terrains.isEmpty()) {
      for (MapPosition pos : influence.influencedTiles)
        if (terrains.contains(map.getTerrainAt(pos)))
          progressPerTurn += PROGRESS_PER_TILE;
    }

    return DISCOVERY_THRESHOLD / progressPerTurn;
  }

  public String previousString(Research next) {
    if (next.previous.isEmpty())
      return "NOTHING";

    StringBuilder sb = new StringBuilder();
    for (String previous : next.previous)
      sb.append(previous).append(", ");
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<String> effectsStrings(Discovery d) {
    List<String> list = new ArrayList<>();
    for (Entry<String, Object> effect : d.effects.entrySet()) {
      String name = effect.getKey();
      Effect e = EFFECTS.get(name);
      list.add(e.toString(effect.getValue()));
    }
    if (buildings.containsKey(d.name))
      list.add("building: " + d.name);
    return list;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static ObjectFloatMap<Faction> getFactionsScores(Discovery discovery) {
    ObjectFloatMap<Faction> scores = new ObjectFloatMap<>(Faction.values().length);
    for (Entry<String, Object> effect : discovery.effects.entrySet()) {
      String name = effect.getKey();
      Effect e = EFFECTS.get(name);
      e.addFactionsScores(scores, effect.getValue());
    }
    for (String group : discovery.groups) {
      if (Policy.get(group) != null) {
        scores.getAndIncrement(Faction.CULTURAL, 0, 1);
        scores.getAndIncrement(Faction.ECONOMIC, 0, 0.5f);
      }
    }
    if (scores.size == 0)
      scores.getAndIncrement(Faction.CULTURAL, 0, 1);
    return scores;
  }

}
