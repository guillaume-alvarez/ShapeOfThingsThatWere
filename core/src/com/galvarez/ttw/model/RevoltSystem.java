package com.galvarez.ttw.model;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparingInt;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.EventsSystem.EventHandler;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.data.Culture;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools.Border;
import com.galvarez.ttw.rendering.FadingMessageRenderSystem;
import com.galvarez.ttw.rendering.IconsSystem.Type;
import com.galvarez.ttw.rendering.NotificationsSystem;
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
public final class RevoltSystem extends EntitySystem implements EventHandler {

  private static final Logger log = LoggerFactory.getLogger(RevoltSystem.class);

  private ComponentMapper<Empire> empires;

  private ComponentMapper<ArmyCommand> army;

  private ComponentMapper<Policies> policies;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<AIControlled> ai;

  private DiscoverySystem discoveries;

  private NotificationsSystem notifications;

  private FadingMessageRenderSystem fadingSystem;

  private final GameMap map;

  private final SessionSettings settings;

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public RevoltSystem(GameMap gameMap, SessionSettings settings, OverworldScreen screen) {
    super(Aspect.getAspectForAll(InfluenceSource.class));
    this.map = gameMap;
    this.settings = settings;
    this.screen = screen;
  }

  @Override
  public String getType() {
    return "Revolt";
  }

  @Override
  protected void initialize() {
    super.initialize();

    world.getSystem(EventsSystem.class).addEventType(this);
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> empires) {
    // progress toward next revolt is triggered by EventsSystem
  }

  @Override
  public int getProgress(Entity empire) {
    return max(0, getInstability(empire) / 2);
  }

  public int getInstability(Entity empire) {
    InfluenceSource source = sources.get(empire);
    Policies empirePolicies = policies.get(empire);
    int totalStability = empirePolicies.stability + max(0, army.get(empire).militaryPower);
    return source.influencedTiles.size() - totalStability;
  }

  /**
   * Cities revolt when the source power is above its stability. The higher it
   * is the higher chance it will revolt.
   */
  @Override
  public boolean execute(Entity empire) {
    int instability = getInstability(empire);
    if (instability <= 0)
      return false;

    InfluenceSource source = sources.get(empire);
    // revolt happens, select the tile!
    Optional<Influence> tile = source.influencedTiles.stream() //
        .filter(p -> isValidRevoltTile(empire, p, instability)) //
        .map(p -> map.getInfluenceAt(p)) //
        .min(comparingInt(Influence::getSecondInfluenceDiff));

    if (tile.isPresent()) {
      updateEmpireAfterRevolt(empire, instability, source);

      createRevoltingEmpire(empire, instability, tile.get());
    } else if (instability > 50) {
      updateEmpireAfterRevolt(empire, instability, source);

      log.warn("Found no tile to revolt for {} with instability at {}%, decrease power to {}",
          empire.getComponent(Name.class), instability, source.power());
      if (!ai.has(empire))
        notifications.addNotification(() -> screen.select(empire, false), null, Type.REVOLT,
            "Instability decrease %s power to %s!", empire.getComponent(Description.class), source.power());
    }
    return true;
  }

  private void updateEmpireAfterRevolt(Entity empire, int instability, InfluenceSource source) {
    // decrease power by instability to avoid popping revolts in loop
    policies.get(empire).stability += instability;
    source.setPower(max(max(1, source.power() / 2), source.power() - instability / 5));
  }

  private boolean isValidRevoltTile(Entity empire, MapPosition pos, int instability) {
    if (!map.getTerrainAt(pos).canStart() || map.hasEntity(pos))
      return false;

    if (instability < map.getInfluenceAt(pos).getMaxInfluence())
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
    Culture culture = empires.get(empire).culture;
    Empire data = new Empire(settings.guessColor(), culture, true);
    settings.empires.add(data);
    Entity revoltee = EntityFactory.createEmpire(world, inf.position.x, inf.position.y, culture.newCityName(), data);
    map.setEntity(revoltee, inf.position);

    // get starting power from generating instability
    // ensure it has enough power to control its own tile and resist a bit
    int sourcePower = sources.get(empire).power();
    sources.get(revoltee).setPower(max(min(sourcePower, instability + sourcePower / 2), inf.getMaxInfluence() + 1));
    inf.setInfluence(revoltee, sources.get(revoltee).power());

    // do not forget neighboring tiles
    for (MapPosition n : map.getNeighbors(inf.position))
      map.getInfluenceAt(n).setInfluence(revoltee, min(sourcePower, instability + sourcePower / 2));

    // add all discoveries from original empire
    discoveries.copyDiscoveries(empire, revoltee);

    // notify player
    log.info("{} revolted from {} (instability={})", revoltee.getComponent(Description.class),
        empire.getComponent(Description.class), instability);
    fadingSystem.createFadingIcon(Type.REVOLT, empires.get(revoltee).color, positions.get(revoltee), 3f);
    if (!ai.has(empire))
      notifications.addNotification(() -> screen.select(revoltee, false), null, Type.REVOLT, "%s revolted from %s!",
          revoltee.getComponent(Description.class), empire.getComponent(Description.class));
  }

}
