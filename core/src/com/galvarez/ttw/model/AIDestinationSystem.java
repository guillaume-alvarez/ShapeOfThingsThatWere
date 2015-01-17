package com.galvarez.ttw.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

@Wire
public final class AIDestinationSystem extends EntityProcessingSystem {

  private static final Logger log = LoggerFactory.getLogger(AIDestinationSystem.class);

  private final GameMap map;

  private ComponentMapper<Destination> destinations;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<AIControlled> intelligences;

  private DestinationSystem destinationSystem;

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public AIDestinationSystem(GameMap gameMap, OverworldScreen screen) {
    super(Aspect.getAspectForAll(AIControlled.class, Destination.class));
    this.map = gameMap;
    this.screen = screen;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity e) {
    Destination dest = destinations.get(e);
    AIControlled ai = intelligences.get(e);

    if (dest.target != null && screen.getTurnNumber() > ai.lastMove + 20)
      return;

    // ...must find another tile to influence
    int bestScore = 0;
    MapPosition best = null;
    for (MapPosition p : destinationSystem.getTargetTiles(e)) {
      int score = estimate(e, p);
      if (score > bestScore) {
        bestScore = score;
        best = p;
      }
    }

    if (best != null && !best.equals(positions.get(e))) {
      dest.target = best;
      dest.path = destinationSystem.computePath(e, dest);
      ai.lastMove = screen.getTurnNumber();
    } else
      log.error("Cannot find a destination for {}", e.getComponent(Description.class));
  }

  private int estimate(Entity e, MapPosition p) {
    int score = 0;
    for (MapPosition n : MapTools.getNeighbors(p.x, p.y, 2)) {
      Entity ne = map.getEntityAt(n);
      if (ne != null && ne != e)
        score -= 10;
      Terrain terrain = map.getTerrainAt(p);
      if (terrain == Terrain.GRASSLAND || terrain == Terrain.PLAIN)
        score += 1;
      Influence inf = map.getInfluenceAt(p);
      if (!inf.isMainInfluencer(e))
        score += 1;
    }
    return score;
  }
}
