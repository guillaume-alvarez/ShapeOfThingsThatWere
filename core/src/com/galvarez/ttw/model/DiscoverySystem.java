package com.galvarez.ttw.model;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.IntIntMap.Entry;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.EventsSystem.EventHandler;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.Policy;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.IconsSystem.Type;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.components.Description;
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
public final class DiscoverySystem extends EntitySystem implements EventHandler {

  private static final Logger log = LoggerFactory.getLogger(DiscoverySystem.class);

  /** Increase to speed progress up. */
  private static final int PROGRESS_PER_TILE = 1;

  private final OverworldScreen screen;

  private final Map<String, Discovery> discoveries;

  /** Discoveries free to be discovered by any empire. */
  private final Map<String, Discovery> toDiscover;

  private final GameMap map;

  private final Random rand = new Random();

  private NotificationsSystem notifications;

  private EffectsSystem effects;

  private SpecialDiscoveriesSystem special;

  private ComponentMapper<Discoveries> empires;

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<InfluenceSource> influences;

  private ComponentMapper<AIControlled> ai;

  @SuppressWarnings("unchecked")
  public DiscoverySystem(SessionSettings s, GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(Discoveries.class));
    this.discoveries = s.getDiscoveries();
    this.toDiscover = new HashMap<>(discoveries);
    this.map = map;
    this.screen = screen;
  }

  @Override
  public String getType() {
    return "Discovery";
  }

  @Override
  protected void initialize() {
    super.initialize();

    special.checkDiscoveries(discoveries);

    // check previous exist
    // set factions scores
    Set<String> groups = new HashSet<>();
    for (Discovery d : discoveries.values())
      groups.addAll(d.groups);
    for (Discovery d : discoveries.values()) {
      d.factions = getFactionsScores(d);

      for (Set<String> list : d.previous)
        for (String previous : list)
          if (!discoveries.containsKey(previous) && !groups.contains(previous))
            log.warn("Cannot find previous {} for discovery {}", previous, d);
    }

    world.getSystem(EventsSystem.class).addEventType(this);
  }

  private ObjectFloatMap<Faction> getFactionsScores(Discovery discovery) {
    ObjectFloatMap<Faction> scores = effects.getFactionsScores(discovery.effects);
    for (String group : discovery.groups)
      if (Policy.get(group) != null) {
        for (Faction f : Faction.values())
          scores.put(f, scores.get(f, 0f) * 1.5f);
        scores.getAndIncrement(Faction.CULTURAL, 0, 0.5f);
      }
    if (scores.size == 0)
      scores.getAndIncrement(Faction.CULTURAL, 0, 0.5f);
    return scores;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    Discoveries d = empires.get(e);
    d.nextPossible = possibleDiscoveries(e, d);
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    // progress toward next discovery is triggered by EventsSystem
  }

  /**
   * Compute the possible discoveries for the empire, associated to the faction
   * that recommends them.
   */
  private Map<Faction, Research> possibleDiscoveries(Entity empire, Discoveries labs) {
    // collect current state
    Set<String> done = new HashSet<>();
    Map<String, List<String>> groups = new HashMap<>();
    for (Discovery d : labs.done) {
      done.add(d.name);
      done.addAll(d.groups);
      for (String g : d.groups) {
        List<String> list = groups.get(g);
        if (list == null)
          groups.put(g, list = new ArrayList<>());
        list.add(d.name);
      }
    }

    Set<Discovery> toDiscover = new HashSet<>(this.toDiscover.values());

    // can only discover discoveries from already known groups if discovered by
    // neighbors, avoid having the same empire discovering all cereals
    toDiscover.removeIf(d -> !d.groups.isEmpty() && groups.keySet().containsAll(d.groups));

    // add discoveries already made by neighboring empires
    for (Entity neighbor : getNeighbors(empire))
      for (Discovery d : empires.get(neighbor).done)
        if (!done.contains(d.name))
          toDiscover.add(d);

    // select new possible discoveries
    List<Research> possible = toDiscover.stream().filter(d -> !done.contains(d.name) && hasTerrain(empire, d.terrains))
        .map(d -> toResearch(d, done, groups)).filter(Objects::nonNull).collect(toList());

    Map<Faction, Research> res = new EnumMap<>(Faction.class);
    for (Faction f : Faction.values()) {
      if (possible.isEmpty()) {
        log.debug("Found no {} research among {} for {}", f, possible, empire.getComponent(Description.class));
        continue;
      }

      Collections.sort(possible, new Comparator<Research>() {
        @Override
        public int compare(Research d1, Research d2) {
          // reverse order: greatest first
          return -Float.compare(d1.factions.get(f, 0f), d2.factions.get(f, 0f));
        }
      });

      Research selected = null;
      for (int i = rand.nextInt(min(2, possible.size())); i >= 0 && selected == null; i--)
        if (possible.get(i).factions.get(f, 0f) >= 0f)
          selected = possible.remove(i);

      if (selected != null)
        res.put(f, selected);
      else
        log.info("Found no {} research among {} for {}", f, possible, empire.getComponent(Description.class));
    }
    return res;
  }

  private Research toResearch(Discovery d, Set<String> done, Map<String, List<String>> groups) {
    List<Discovery> previous = new ArrayList<>(d.previous.size());
    for (Collection<String> l : d.previous)
      if (done.containsAll(l)) {
        for (String p : l) {
          List<String> group = groups.get(p);
          previous.add(discoveries.get(group == null ? p : group.get(rand.nextInt(group.size()))));
        }
        return new Research(d, previous);
      }
    return null;
  }

  private boolean hasTerrain(Entity entity, Set<Terrain> terrains) {
    if (terrains == null || terrains.isEmpty())
      return true;

    InfluenceSource influence = influences.get(entity);
    for (MapPosition pos : influence.influencedTiles)
      if (terrains.contains(map.getTerrainAt(pos)))
        return true;

    return false;
  }

  private Iterable<Entity> getNeighbors(Entity empire) {
    IntMap<Entity> neighbors = new IntMap<>();

    // collect entities we have relations with
    for (Entity e : relations.get(empire).relations.keySet())
      neighbors.put(e.getId(), e);

    // collect entities we share influence with
    for (MapPosition p : influences.get(empire).influencedTiles)
      for (Entry inf : map.getInfluenceAt(p))
        if (inf.key != empire.getId() && !neighbors.containsKey(inf.key)
        // may be pending insertion into world if just revolted
            && world.getEntityManager().isActive(inf.key))
          neighbors.put(inf.key, world.getEntity(inf.key));

    return neighbors.values();
  }

  public List<String> effectsStrings(Discovery d) {
    return effects.toString(d.effects);
  }

  public void copyDiscoveries(Entity from, Entity to) {
    Discoveries fromD = empires.get(from);
    Discoveries toD = empires.get(to);
    for (Discovery d : fromD.done)
      if (toD.done.add(d)) {
        effects.apply(d.effects, to, false);
        special.apply(to, d, toD);
      }
  }

  @Override
  public int getProgress(Entity e) {
    Discoveries discovery = empires.get(e);
    InfluenceSource influence = influences.get(e);

    int progress = discovery.progressPerTurn;
    Set<Terrain> terrains = discovery.last != null ? discovery.last.target.terrains : null;
    if (terrains != null && !terrains.isEmpty()) {
      for (MapPosition pos : influence.influencedTiles) {
        if (terrains.contains(map.getTerrainAt(pos)))
          progress += PROGRESS_PER_TILE;
      }
    }
    return progress;
  }

  @Override
  public boolean execute(Entity e) {
    return discoverNext(e, empires.get(e));
  }

  private boolean discoverNext(Entity empire, Discoveries discovery) {
    discovery.nextPossible = possibleDiscoveries(empire, discovery);

    if (discovery.nextPossible.isEmpty())
      return false;

    if (!ai.has(empire)) {
      Research last = discovery.last;
      notifications.addNotification(screen::askDiscovery, () -> discovery.last != last, Type.DISCOVERY,
          "We can make a new discovery!");
    }
    return true;
  }

  /** Called when the next discovery is chosen. */
  public void discoverNew(Entity empire, Discoveries discovery, Research research) {
    Discovery target = research.target;
    log.info("{} discovered {} from {}.", empire.getComponent(Name.class), target, research.previous);
    discovery.done.add(target);
    discovery.nextPossible.clear();

    if (!ai.has(empire)) {
      Set<Policy> policies = PoliciesSystem.getPolicies(target);
      if (policies != null) {
        for (Policy policy : policies)
          notifications
              .addNotification(screen::policiesMenu, null, Type.DISCOVERY, "New %s policy: %s", policy, target);
      }
    }

    // Once an empire made a discovery, none other can research it. Those
    // already researching it can continue. It makes sure starting discoveries
    // are not found only by a single empire.
    if (!target.previous.isEmpty())
      toDiscover.remove(target.name);

    // remove last discovery 2x bonus (to keep only the basic effect)
    if (discovery.last != null)
      effects.apply(discovery.last.target.effects, empire, true);
    discovery.last = research;

    // apply new discovery
    effects.apply(target.effects, empire, false);
    // ... and bonus time until next discovery!
    effects.apply(target.effects, empire, false);

    // may be some 'special discovery'
    special.apply(empire, target, discovery);

    MapPosition pos = empire.getComponent(MapPosition.class);
    EntityFactory.createFadingTileLabel(world, target.name, empire.getComponent(Empire.class).color, pos.x, pos.y, 3f);
  }

}
