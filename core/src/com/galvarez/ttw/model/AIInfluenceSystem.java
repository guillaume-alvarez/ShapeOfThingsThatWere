package com.galvarez.ttw.model;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

@Wire
public final class AIInfluenceSystem extends EntityProcessingSystem {
  
  private static final Logger log = LoggerFactory.getLogger(AIInfluenceSystem.class);

  private final GameMap map;

  private final OverworldScreen screen;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<AIControlled> intelligence;

  private InfluenceSystem influenceSystem;

  @SuppressWarnings("unchecked")
  public AIInfluenceSystem(GameMap gameMap, OverworldScreen screen) {
    super(Aspect.getAspectForAll(AIControlled.class, InfluenceSource.class));
    this.map = gameMap;
    this.screen = screen;
  }

  @Override
  protected void inserted(Entity e) {
    AIControlled ia = intelligence.get(e);
    MapPosition pos = positions.get(e);

    ia.estimatedTiles = estimateFlaggableTiles(e, pos);
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity e) {
    AIControlled ia = intelligence.get(e);
    InfluenceSource source = sources.get(e);

    flagTile(e, ia, source);
  }

  private void flagTile(Entity e, AIControlled ia, InfluenceSource source) {
    /*
     * At the moment the AI values all possible flag target depending on range
     * and cost. It selects the tile with the lowest range * cost. Then it let
     * the flag on the tile until it is influenced.
     */
    if (source.target != null) {
      if (stillInterestedInTile(e, source.target, source))
        // continue influencing that case only if not competing with another IA
        return;
      // else...
    }

    // ...must find another tile to influence
    ia.estimatedTiles = estimateFlaggableTiles(e, positions.get(e));

    while (ia.estimatedTiles.size() > 0) {
      MapPosition pos = ia.estimatedTiles.remove(0);
      if (stillInterestedInTile(e, pos, source)) {
        screen.flag(e, pos.x, pos.y);
        return;
      }
    }
    log.error("Cannot flag anything from {}", e.getComponent(Description.class));
  }

  private boolean stillInterestedInTile(Entity e, MapPosition pos, InfluenceSource source) {
    Influence inf = map.getInfluenceAt(pos);

    if (inf.isMainInfluencer(e))
      return false;
    if (!influenceSystem.canInfluence(e, pos))
      return false;

    // no need to influence other city in same empire
    int main = inf.getMainInfluenceSource();
    return main == -1 || sources.get(world.getEntity(main)).empire != source.empire;
  }

  private List<MapPosition> estimateFlaggableTiles(Entity e, MapPosition pos) {
    Map<MapPosition, Integer> distances = influenceSystem.getDistances(e, pos);
    return distances.entrySet().stream().sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
        .map(Map.Entry::getKey).collect(toList());
  }
}
