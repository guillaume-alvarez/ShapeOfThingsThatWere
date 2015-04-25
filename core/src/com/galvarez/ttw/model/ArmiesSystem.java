package com.galvarez.ttw.model;

import java.util.Iterator;
import java.util.List;

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
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.IconsSystem.Type;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.components.Counter;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.screens.overworld.OverworldScreen;
import com.galvarez.ttw.utils.RomanNumbers;

/**
 * Moves the entities being armies across the map.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class ArmiesSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(ArmiesSystem.class);

  private ComponentMapper<Counter> counters;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<ArmyCommand> commands;

  private ComponentMapper<Army> armies;

  private ComponentMapper<Description> descriptions;

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
      for (Iterator<Entity> it = source.secondarySources.iterator(); it.hasNext();) {
        Entity s = it.next();
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
          MapPosition newPos = emptyPositionNextTo(city);
          if (newPos != null) {
            destinationSystem.moveTo(s, newPos);
            if (!ai.has(city))
              notifications.addNotification(() -> screen.select(s, true), null, Type.MILITARY,
                  "Depleted %s moved to capital.", names.get(s));
          } else {
            s.deleteFromWorld();
            it.remove();
            if (!ai.has(city))
              notifications.addNotification(() -> screen.select(s, true), null, Type.MILITARY,
                  "Depleted %s destroyed.", names.get(s));
          }
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
    InfluenceSource source = sources.get(empire);
    return source.secondarySources;
  }

  public Entity createNewArmy(Entity empire, int power) {
    MapPosition pos = emptyPositionNextTo(empire);
    if (pos != null) {
      ArmyCommand command = commands.get(empire);
      Entity army = EntityFactory.createArmy(world, pos, armyName(command, empire), empires.get(empire), empire, power);
      sources.get(empire).secondarySources.add(army);
      command.usedPower += power;
      map.setEntity(army, pos);
      return army;
    } else {
      log.warn("Cannot create an army near {}, no empty tile.", descriptions.get(empire));
      return null;
    }
  }

  private String armyName(ArmyCommand command, Entity empire) {
    int nb = ++command.counter;
    return "Army of " + names.get(empire).name + " " + RomanNumbers.toRoman(nb);
  }

  private MapPosition emptyPositionNextTo(Entity empire) {
    MapPosition pos = positions.get(empire);
    for (int dist = 1; dist < 4; dist++) {
      for (MapPosition neighbor : map.getNeighbors(pos.x, pos.y, dist))
        if (!map.hasEntity(neighbor) && map.getInfluenceAt(neighbor).isMainInfluencer(empire))
          return neighbor;
    }
    // should rarely happen
    return null;
  }
}
