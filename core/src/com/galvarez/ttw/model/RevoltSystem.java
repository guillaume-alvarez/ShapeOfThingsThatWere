package com.galvarez.ttw.model;

import static java.util.Comparator.comparingInt;

import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.data.Culture;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools.Border;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.NotificationsSystem.Type;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * This classes check if new empires should revolt from existing ones with
 * negative stability.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class RevoltSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(RevoltSystem.class);

  private ComponentMapper<Empire> empires;

  private ComponentMapper<Policies> policies;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<AIControlled> ai;

  private DiscoverySystem discoveries;

  private NotificationsSystem notifications;

  private final GameMap map;

  private final SessionSettings settings;

  private final Random rand = new Random();

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public RevoltSystem(GameMap gameMap, SessionSettings settings, OverworldScreen screen) {
    super(Aspect.getAspectForAll(InfluenceSource.class));
    this.map = gameMap;
    this.settings = settings;
    this.screen = screen;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> empires) {
    for (Entity empire : empires) {
      InfluenceSource source = sources.get(empire);
      checkRevolt(empire, source);
    }
  }

  /**
   * Cities revolt when the source power is above its stability. The higher it
   * is the higher chance it will revolt.
   */
  private void checkRevolt(Entity empire, InfluenceSource source) {
    Policies empirePolicies = policies.get(empire);
    int instability = source.power - empirePolicies.stability;
    if (instability > 0 && rand.nextInt(100) < instability) {
      // revolt happens, select the tile!
      Optional<Influence> tile = source.influencedTiles.stream() //
          .filter(p -> isValidRevoltTile(empire, p)) //
          .map(p -> map.getInfluenceAt(p)) //
          .min(comparingInt(Influence::getSecondInfluenceDiff));
      if (tile.isPresent()) {
        Influence inf = tile.get();
        createRevoltingEmpire(empire, instability, inf);
      } else {
        log.warn("Found no tile to revolt for {} with instability at {}%, decrease power to {}",
            empire.getComponent(Name.class), instability, source.power);
        if (!ai.has(empire))
          notifications.addNotification(() -> screen.select(empire, false), null, Type.REVOLT,
              "Instability decrease %s power to %s!", empire.getComponent(Description.class), source.power);
      }
      // decrease power of stability to avoid popping revolts in loop
      source.power -= instability;
    }
  }

  private boolean isValidRevoltTile(Entity empire, MapPosition pos) {
    if (!map.getTerrainAt(pos).canStart() || map.hasEntity(pos))
      return false;

    boolean atInfluenceBorder = false;
    for (Border b : Border.values()) {
      MapPosition neighbor = b.getNeighbor(pos);
      // don't accept tiles on map border
      if (!map.isOnMap(neighbor))
        return false;

      // don't accept near other entities
      if (map.hasEntity(neighbor))
        return false;

      // accept if different overlord
      atInfluenceBorder |= !map.getInfluenceAt(neighbor).isMainInfluencer(empire);
    }
    return atInfluenceBorder;
  }

  private void createRevoltingEmpire(Entity empire, int instability, Influence inf) {
    Entity mainInfluence = world.getEntity(inf.getMainInfluenceSource());
    Culture culture = empires.get(empire).culture;
    Empire data = new Empire(settings.guessColor(), culture, true);
    settings.empires.add(data);
    Entity revoltee = EntityFactory.createEmpire(world, inf.position.x, inf.position.y, culture.newCityName(), data);
    inf.setInfluence(revoltee, inf.getMaxInfluence() + inf.getDelta(mainInfluence) + instability);

    // get starting power from generating instability
    sources.get(revoltee).power = instability;

    // do not forget neighboring tiles
    for (MapPosition n : map.getNeighbors(inf.position)) {
      Influence neighbor = map.getInfluenceAt(n);
      neighbor.setInfluence(revoltee, instability + neighbor.getInfluence(mainInfluence));
    }

    // add all discoveries from original empire
    discoveries.copyDiscoveries(empire, revoltee);

    // notify player
    log.info("Created revolting {}", revoltee.getComponent(Description.class));
    if (!ai.has(empire))
      notifications.addNotification(() -> screen.select(revoltee, false), null, Type.REVOLT, "%s revolted from %s!",
          revoltee.getComponent(Description.class), empire.getComponent(Description.class));
  }

}
