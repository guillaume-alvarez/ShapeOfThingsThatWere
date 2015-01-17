package com.galvarez.ttw.model.map;

import static java.lang.Math.max;

import java.util.Iterator;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntIntMap.Entry;

public final class Influence implements Iterable<IntIntMap.Entry> {

  public final Terrain terrain;

  private final IntIntMap influence = new IntIntMap();

  private final IntIntMap influenceDelta = new IntIntMap();

  private int mainInfluence = 0;

  private int mainInfluenceSource = -1;

  public final MapPosition position;

  public Influence(MapPosition position, Terrain terrain) {
    this.position = position;
    this.terrain = terrain;
  }

  /** Set the source influence to the tile. */
  public void setInfluence(Entity source, int inf) {
    influence.put(source.getId(), inf);
    recomputeMain();
  }

  /** Add to the delta for source influence on next turn to the tile. */
  public void addInfluenceDelta(Entity source, int delta) {
    influenceDelta.getAndIncrement(source.getId(), 0, delta);
  }

  public Iterable<IntIntMap.Entry> getDelta() {
    return influenceDelta;
  }

  public int getDelta(Entity source) {
    return influenceDelta.get(source.getId(), 0);
  }

  /** Apply stored delta on influence. */
  public void applyDelta() {
    for (Entry e : influenceDelta) {
      int old = influence.get(e.key, 0);
      influence.put(e.key, max(0, e.value + old));
    }
    influenceDelta.clear();
    recomputeMain();
  }

  private void recomputeMain() {
    mainInfluence = terrain.moveCost() - 1;
    mainInfluenceSource = -1;
    if (influence.size > 0) {
      for (Entry e : influence) {
        if (e.value > mainInfluence) {
          mainInfluenceSource = e.key;
          mainInfluence = e.value;
        }
      }
    }
  }

  public int getInfluence(Entity e) {
    return influence.get(e.getId(), 0);
  }

  public int getMainInfluenceSource() {
    return mainInfluenceSource;
  }

  public Entity getMainInfluenceSource(World world) {
    if (hasMainInfluence())
      return world.getEntity(mainInfluenceSource);
    else
      return null;
  }

  public int requiredInfluence(Entity e) {
    if (isMainInfluencer(e))
      return 0;
    int current = influence.get(e.getId(), 0);
    return max(terrain.moveCost(), mainInfluence + 1) - current;
  }

  public boolean isMainInfluencer(Entity e) {
    return e.getId() == mainInfluenceSource;
  }

  public boolean hasMainInfluence() {
    return mainInfluenceSource >= 0;
  }

  public boolean hasInfluence(Entity source) {
    return influence.get(source.getId(), 0) > 0;
  }

  public int getMaxInfluence() {
    int max = 0;
    for (Entry e : influence)
      if (e.value > max)
        max = e.value;
    return max(max, terrain.moveCost());
  }

  public int getTotalInfluence() {
    int total = 0;
    for (Entry e : influence)
      total += e.value;
    return max(total, terrain.moveCost());
  }

  @Override
  public Iterator<Entry> iterator() {
    return influence.iterator();
  }

  @Override
  public String toString() {
    return terrain + influence.toString();
  }

}
