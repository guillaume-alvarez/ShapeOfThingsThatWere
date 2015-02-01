package com.galvarez.ttw.model.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFinding {

  private static final Logger log = LoggerFactory.getLogger(PathFinding.class);

  private final GameMap map;

  public PathFinding(GameMap map) {
    this.map = map;
  }

  private static int heuristic(MapPosition a, MapPosition b) {
    return MapTools.distance(a, b) * Terrain.FOREST.moveCost();
  }

  private static final class Pos implements Comparable<Pos> {

    private final MapPosition p;

    private final int s;

    public Pos(MapPosition p, int s) {
      this.p = p;
      this.s = s;
    }

    @Override
    public int compareTo(Pos o) {
      return Integer.compare(s, o.s);
    }

    @Override
    public String toString() {
      return p.toString() + '=' + s;
    }
  }

  public List<MapPosition> aStarSearch(MapPosition start, MapPosition goal, Predicate<MapPosition> canMoveTo) {
    PriorityQueue<Pos> frontier = new PriorityQueue<Pos>();
    frontier.add(new Pos(start, 0));
    Map<MapPosition, MapPosition> cameFrom = new HashMap<>();
    Map<MapPosition, Integer> costSoFar = new HashMap<>();
    cameFrom.put(start, null);
    costSoFar.put(start, 0);

    while (!frontier.isEmpty()) {
      Pos current = frontier.poll();

      if (current.p.equals(goal))
        break;

      for (MapPosition next : neighbors(current.p, canMoveTo)) {
        int newCost = costSoFar.get(current.p) + cost(current.p, next);
        if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
          costSoFar.put(next, newCost);
          frontier.add(new Pos(next, newCost + heuristic(goal, next)));
          cameFrom.put(next, current.p);
        }
      }
    }
    return path(cameFrom, start, goal);
  }

  private int cost(MapPosition p, MapPosition next) {
    // TODO should depend on influence cost for empire
    // TODO start is important: on water first step costs the most
    int cost = map.getTerrainAt(next).moveCost();
    if (!map.getEntitiesAt(p).isEmpty())
      return cost * 2;
    else
      return cost;
  }

  private Iterable<MapPosition> neighbors(MapPosition p, Predicate<MapPosition> canMoveTo) {
    List<MapPosition> list = new ArrayList<>();
    for (MapPosition n : map.getNeighbors(p))
      if (canMoveTo.test(n))
        list.add(n);
    return list;
  }

  private static List<MapPosition> path(Map<MapPosition, MapPosition> cameFrom, MapPosition start, MapPosition goal) {
    List<MapPosition> path = new ArrayList<>();
    MapPosition current = goal;
    while (!current.equals(start)) {
      path.add(current);
      current = cameFrom.get(current);
      if (current == null) {
        log.warn("Cannot compute path from {} to {}.", start, goal);
        return null;
      }
    }
    Collections.reverse(path);
    return path;
  }
}
