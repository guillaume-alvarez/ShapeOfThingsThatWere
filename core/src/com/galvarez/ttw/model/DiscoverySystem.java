package com.galvarez.ttw.model;

import static java.lang.Math.max;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Map.Entry;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.NotificationsSystem;
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

  private static final int DISCOVERY_THRESHOLD = 100;

  private final OverworldScreen screen;

  private final Map<String, Discovery> discoveries;

  private final GameMap map;

  private NotificationsSystem notifications;

  private ComponentMapper<Discoveries> empires;

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
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity entity : entities) {
      Discoveries discovery = empires.get(entity);
      if (discovery.next != null) {
        InfluenceSource influence = getInfluence(entity);
        if (progressNext(entity, discovery, influence))
          discoverNext(entity, discovery, influence);
      }
    }
  }

  private boolean progressNext(Entity entity, Discoveries discovery, InfluenceSource influence) {
    int progress = 0;
    Set<Terrain> terrains = discovery.next.target.terrains;
    if (terrains == null || terrains.isEmpty())
      progress = influence.influencedTiles.size();
    else {
      for (MapPosition pos : influence.influencedTiles) {
        if (terrains.contains(map.getTerrainAt(pos)))
          progress++;
      }
    }
    discovery.next.progress += max(1, progress);
    return discovery.next.progress >= DISCOVERY_THRESHOLD;
  }

  private InfluenceSource getInfluence(Entity empire) {
    Entity capital = capitals.get(empire).capital;
    InfluenceSource influence = influences.get(capital);
    return influence;
  }

  private void discoverNext(Entity entity, Discoveries discovery, InfluenceSource influence) {
    Research next = discovery.next;
    log.info("{} discovered {} from {}.", entity.getComponent(Name.class), next.target, next.previous);
    if (!ai.has(entity))
      notifications.addNotification(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          screen.discoveryMenu();
        }
      }, "Discovery!", "You discovered %s from %s.", next.target, previousString(discovery, next.target));
    discovery.done.add(next.target);
    discovery.next = null;

    applyDiscoveryEffects(next.target, influence);
  }

  private void applyDiscoveryEffects(Discovery discovery, InfluenceSource influence) {
    for (Entry<String, Number> effect : discovery.effects.entrySet()) {
      Terrain t = Terrain.valueOf(effect.getKey());
      int delta = effect.getValue().intValue();
      Integer current = influence.modifiers.terrainBonus.get(t);
      influence.modifiers.terrainBonus.put(t, current == null ? delta : current + delta);
    }
  }

  /**
   * Compute the 'nb' first possible discoveries for the empire, associated to
   * the expected number of turns to get them.
   */
  public Map<Discovery, Integer> possibleDiscoveries(Entity entity, Discoveries empire, int nb) {
    Set<String> done = empire.done.stream().map(Discovery::getName).collect(toSet());
    empire.done.forEach(d -> done.addAll(d.groups));

    Predicate<Discovery> canBeDiscovered = d -> !done.contains(d.name) && done.containsAll(d.previous)
        && hasTerrain(entity, d.terrains);
    return discoveries.values().stream().filter(canBeDiscovered).limit(nb)
        .collect(toMap(d -> d, d -> guessNbTurns(entity, d.terrains)));
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

  private int guessNbTurns(Entity empire, Set<Terrain> terrains) {
    InfluenceSource influence = getInfluence(empire);

    int progressPerTurn = 0;
    if (terrains == null || terrains.isEmpty()) {
      progressPerTurn = influence.influencedTiles.size();
    } else {
      for (MapPosition pos : influence.influencedTiles)
        if (terrains.contains(map.getTerrainAt(pos)))
          progressPerTurn++;
    }

    return DISCOVERY_THRESHOLD / max(1, progressPerTurn);
  }

  public String previousString(Discoveries empire, Discovery next) {
    if (next.previous.isEmpty())
      return "NOTHING";

    StringBuilder sb = new StringBuilder();
    for (String previous : next.previous) {
      if (previous.matches("[A-Z]+"))
        sb.append(getDiscoveryForGroup(empire, previous).name);
      else
        sb.append(previous);
      sb.append(", ");
    }
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

  private Discovery getDiscoveryForGroup(Discoveries empire, String previous) {
    for (Discovery d : empire.done)
      if (d.groups.contains(previous)) {
        return d;
      }
    return null;
  }
}
