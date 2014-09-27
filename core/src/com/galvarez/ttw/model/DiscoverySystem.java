package com.galvarez.ttw.model;

import static java.lang.Math.max;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
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

  private final Map<String, Discovery> discoveries;

  private final GameMap map;

  private NotificationsSystem notifications;

  private ComponentMapper<Discoveries> empires;

  private ComponentMapper<Capital> capitals;

  private ComponentMapper<InfluenceSource> influences;

  private ComponentMapper<AIControlled> ai;

  @SuppressWarnings("unchecked")
  public DiscoverySystem(Map<String, Discovery> discoveries, GameMap map) {
    super(Aspect.getAspectForAll(Discoveries.class, Capital.class));
    this.discoveries = discoveries;
    this.map = map;
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
        if (progressNext(entity, discovery))
          discoverNext(entity, discovery);
      }
    }
  }

  private boolean progressNext(Entity entity, Discoveries discovery) {
    Entity capital = capitals.get(entity).capital;
    InfluenceSource influence = influences.get(capital);
    int progress = 0;
    List<Terrain> terrains = discovery.next.target.terrains;
    if (terrains == null || terrains.isEmpty())
      progress = influence.influencedTiles.size();
    else {
      Set<Terrain> set = EnumSet.copyOf(terrains);
      for (MapPosition pos : influence.influencedTiles) {
        if (set.contains(map.getTerrainAt(pos)))
          progress++;
      }
    }
    discovery.next.progress += max(1, progress);
    return discovery.next.progress >= 100;
  }

  private void discoverNext(Entity entity, Discoveries discovery) {
    Research next = discovery.next;
    System.out.printf("%s discoved %s from %s.\n", entity.getComponent(Name.class), next.target, next.previous);
    if (!ai.has(entity))
      notifications.addNotification("Discovery!", "You discovered %s from %s.", next.target,
          previousString(discovery, next));
    discovery.done.add(next.target);
    discovery.next = null;
  }

  public List<Research> possibleDiscoveries(Entity entity, Discoveries empire, int nb) {
    Set<String> done = empire.done.stream().map(Discovery::getName).collect(toSet());
    empire.done.forEach(d -> done.addAll(d.groups));

    Predicate<Discovery> canBeDiscovered = d -> !done.contains(d.name) && done.containsAll(d.previous)
        && hasTerrain(entity, d.terrains);
    return discoveries.values().stream().filter(canBeDiscovered).limit(nb)
        .collect(ArrayList<Research>::new, (l, d) -> l.add(new Research(d.previous, d)), ArrayList::addAll);
  }

  private boolean hasTerrain(Entity entity, List<Terrain> terrains) {
    if (terrains == null || terrains.isEmpty())
      return true;

    Set<Terrain> set = EnumSet.copyOf(terrains);
    Entity capital = capitals.get(entity).capital;
    InfluenceSource influence = influences.get(capital);
    for (MapPosition pos : influence.influencedTiles)
      if (set.contains(map.getTerrainAt(pos)))
        return true;

    return false;
  }

  public String previousString(Discoveries empire, Research next) {
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
