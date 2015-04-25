package com.galvarez.ttw.model;

import java.util.Map;
import java.util.Map.Entry;

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
import com.galvarez.ttw.model.components.Buildings;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Building;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.IconsSystem.Type;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

@Wire
public final class BuildingsSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(BuildingsSystem.class);

  private ComponentMapper<Name> names;

  private ComponentMapper<Buildings> cities;

  private ComponentMapper<Discoveries> discoveries;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Empire> empires;

  private ComponentMapper<AIControlled> ai;

  private ComponentMapper<MapPosition> positions;

  private NotificationsSystem notifications;

  private EffectsSystem effects;

  private final Map<String, Building> buildings;

  private final OverworldScreen screen;

  private final GameMap map;

  @SuppressWarnings("unchecked")
  public BuildingsSystem(OverworldScreen screen, SessionSettings s, GameMap map) {
    super(Aspect.getAspectForAll(InfluenceSource.class, Buildings.class));
    this.screen = screen;
    buildings = s.getBuildings();
    this.map = map;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity empire : entities) {
      Buildings city = cities.get(empire);
      if (city.construction == null) {
        Building next = selectNextBuilding(city, empire);
        if (next != null) {
          city.construction = next;
          city.constructionTurns = 0;
        }
      } else if (++city.constructionTurns >= city.construction.turns) {
        Building newBuilding = city.construction;
        Building oldBuilding = city.built.put(newBuilding.type, newBuilding);
        if (oldBuilding != null)
          effects.apply(oldBuilding.effects, empire, true);
        effects.apply(newBuilding.effects, empire, false);
        city.construction = null;
        city.constructionTurns = 0;
        log.info("{} built {}", names.get(empire), newBuilding);
        MapPosition pos = positions.get(empire);
        EntityFactory.createFadingTileLabel(world, newBuilding.getName(), empires.get(empire).color, pos.x, pos.y, 3f);
        if (!ai.has(empire))
          notifications.addNotification(() -> screen.select(empire, false), null, Type.BUILDINGS, "Built %s in %s.",
              newBuilding, names.get(empire));
      }
    }
  }

  private Building selectNextBuilding(Buildings city, Entity empire) {
    InfluenceSource source = sources.get(empire);
    Discoveries dis = discoveries.get(empire);
    for (Discovery d : dis.done) {
      Building building = buildings.get(d.name);
      if (building != null) {
        if (verifyConditions(building, city, source))
          return building;
      }
    }
    return null;
  }

  private boolean verifyConditions(Building building, Buildings city, InfluenceSource source) {
    // check previous is consistent
    Building current = city.built.get(building.type);
    if (building.previous != null) {
      if (current == null || !current.getName().equals(building.getName()))
        return false;
    } else if (current != null)
      return false;

    for (Entry<String, Object> cond : building.conditions.entrySet()) {
      if ("terrain".equals(cond.getKey())) {
        Terrain terrain = Terrain.valueOf((String) cond.getValue());
        if (!hasTerrain(source, terrain))
          return false;
      } else if ("power".equals(cond.getKey())) {
        return source.power() >= ((Number) cond.getValue()).intValue();
      } else
        throw new IllegalStateException("Unknown condition " + cond);
    }
    return true;
  }

  private boolean hasTerrain(InfluenceSource source, Terrain terrain) {
    for (MapPosition pos : source.influencedTiles)
      if (map.getTerrainAt(pos) == terrain)
        return true;
    return false;
  }

}
