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
    Destination dest = destinations.getSafe(e);
    if (dest == null)
      return;

    AIControlled ai = intelligences.get(e);
    if (dest.target == null) {
      setNewTarget(e, dest, ai);
    } else {
      // check we are not stuck
      MapPosition current = positions.get(e);
      if (current.equals(ai.lastPosition)) {
        // are we stuck for 3 turns?
        if (screen.getTurnNumber() - ai.lastMove > 3)
          setNewTarget(e, dest, ai);
      } else {
        ai.lastMove = screen.getTurnNumber();
        ai.lastPosition = current;
      }
    }
  }

  private void setNewTarget(Entity e, Destination dest, AIControlled ai) {
    // ...must find another tile to influence
    int bestScore = 0;
    MapPosition best = null;
    for (MapPosition p : destinationSystem.getTargetTiles(e)) {
      if (p.equals(dest.target))
        // if it fails, do not select it again
        continue;
      int score = estimate(e, p);
      if (score > bestScore) {
        bestScore = score;
        best = p;
      }
    }

    if (best != null && !best.equals(positions.get(e))) {
      destinationSystem.computePath(e, best);
      ai.lastMove = screen.getTurnNumber();
      ai.lastPosition = positions.get(e);
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
