package com.galvarez.ttw.model;

import java.util.List;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.NotificationsSystem.Type;
import com.galvarez.ttw.rendering.components.Counter;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * Moves the entities being armies across the map.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class ArmiesSystem extends EntitySystem {

  private ComponentMapper<Counter> counters;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Capital> capitals;

  private ComponentMapper<ArmyCommand> commands;

  private ComponentMapper<Army> armies;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Name> names;

  private ComponentMapper<Empire> empires;

  private ComponentMapper<AIControlled> ai;

  private DestinationSystem destinationSystem;

  private NotificationsSystem notifications;

  private final GameMap map;

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public ArmiesSystem(GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(InfluenceSource.class));
    this.map = map;
    this.screen = screen;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity city : entities) {
      InfluenceSource source = sources.get(city);
      for (Entity s : source.secondarySources) {
        Army army = armies.get(s);
        if (onOtherInfluence(city, s)) {
          army.currentPower--;
          if (!ai.has(city))
            notifications.addNotification(() -> screen.select(s, true), null, Type.MILITARY,
                "%s is depleting in other empire!", names.get(s));
        } else {
          if (army.currentPower < army.maxPower)
            army.currentPower++;
          else if (army.currentPower > army.maxPower)
            army.currentPower--;
        }

        if (army.currentPower <= 0) {
          // army is defeated, move it to capital
          army.currentPower = 0;
          destinationSystem.moveTo(s, positions.get(city));
          if (!ai.has(city))
            notifications.addNotification(() -> screen.select(s, true), null, Type.MILITARY,
                "Depleted %s moved to capital.", names.get(s));
        }
        counters.get(s).value = army.currentPower;
      }
    }
  }

  private boolean onOtherInfluence(Entity city, Entity army) {
    Influence inf = map.getInfluenceAt(positions.get(army));
    return inf.hasMainInfluence() && !inf.isMainInfluencer(city);
  }

  public List<Entity> getArmies(Entity empire) {
    InfluenceSource source = sources.get(capitals.get(empire).capital);
    return source.secondarySources;
  }

  public Entity createNewArmy(Entity empire, int power) {
    Capital c = capitals.get(empire);
    MapPosition pos = positions.get(c.capital);
    Entity army = EntityFactory.createArmy(world, pos, names.get(empire).name, empires.get(empire), c.capital, power);
    sources.get(c.capital).secondarySources.add(army);
    commands.get(empire).usedPower += power;
    map.addEntity(army, pos);
    return army;
  }
}
