package com.galvarez.ttw.model;

import java.util.ArrayList;
import java.util.Collections;
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
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Discovery;
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

  private final OverworldScreen screen;

  private final Map<String, Discovery> discoveries;

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
  public DiscoverySystem(Map<String, Discovery> discoveries, GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(Discoveries.class, Capital.class));
    this.discoveries = discoveries;
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
      Condition condition = !possibleDiscoveries(entity, discovery, 1).isEmpty() ? (() -> discovery.next != null)
          : null;
      notifications.addNotification(() -> screen.discoveryMenu(), condition, Type.DISCOVERY, "Discovered %s: %s",
          target, target.effects.isEmpty() ? "No effect." : target.effects.toString());
    }
    discovery.done.add(target);
    discovery.next = null;

    // remove last discovery 2x bonus (to keep only the basic effect)
    if (discovery.last != null)
      applyDiscoveryEffects(discovery.last.target, entity, true);
    discovery.last = next;

    // apply new discovery
    applyDiscoveryEffects(target, entity, false);
    // ... and bonus time until next discovery!
    applyDiscoveryEffects(target, entity, false);
  }

  void applyDiscoveryEffects(Discovery discovery, Entity entity, boolean revert) {
    for (Entry<String, Object> effect : discovery.effects.entrySet()) {
      String name = effect.getKey();
      if ("militaryPower".equalsIgnoreCase(name)) {
        int delta = ((Number) effect.getValue()).intValue();
        Army army = armies.get(entity);
        if (revert)
          army.militaryPower -= delta;
        else
          army.militaryPower += delta;
      } else if ("diplomacy".equalsIgnoreCase(name)) {
        // cannot revert that one
        Diplomacy diplomacy = entity.getComponent(Diplomacy.class);
        diplomacy.knownStates.add(State.valueOf((String) effect.getValue()));
      } else if ("growth".equalsIgnoreCase(name)) {
        InfluenceSource source = getInfluence(entity);
        int delta = ((Number) effect.getValue()).intValue();
        if (revert)
          source.growth -= delta;
        else
          source.growth += delta;
      } else if ("discovery".equalsIgnoreCase(name)) {
        Discoveries empire = empires.get(entity);
        int delta = ((Number) effect.getValue()).intValue();
        if (revert)
          empire.progressPerTurn -= delta;
        else
          empire.progressPerTurn += delta;
      } else if ("stability".equalsIgnoreCase(name)) {
        Policies empire = policies.get(entity);
        int delta = ((Number) effect.getValue()).intValue();
        if (revert)
          empire.stabilityGrowth -= delta;
        else
          empire.stabilityGrowth += delta;
      } else {
        InfluenceSource source = getInfluence(entity);
        Terrain t = Terrain.valueOf(name);
        int delta = ((Number) effect.getValue()).intValue();
        Integer current = source.modifiers.terrainBonus.get(t);
        if (revert)
          source.modifiers.terrainBonus.put(t, current == null ? 0 : current - delta);
        else
          source.modifiers.terrainBonus.put(t, current == null ? delta : current + delta);
      }
    }
  }

  /**
   * Compute the 'nb' first possible discoveries for the empire, associated to
   * the expected number of turns to get them.
   */
  public Map<Research, Integer> possibleDiscoveries(Entity entity, Discoveries empire, int nb) {
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

    Map<Research, Integer> res = new HashMap<>();
    for (int i = 0; i < possible.size() && i < nb; i++) {
      Discovery d = possible.get(i);
      List<String> previous = new ArrayList<>(d.previous.size());
      d.previous.forEach(p -> {
        List<String> group = groups.get(p);
        previous.add(group == null ? p : group.get(rand.nextInt(group.size())));
      });
      res.put(new Research(d, previous), guessNbTurns(empire, entity, d.terrains));
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

  private int guessNbTurns(Discoveries discovery, Entity empire, Set<Terrain> terrains) {
    InfluenceSource influence = getInfluence(empire);

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

}
