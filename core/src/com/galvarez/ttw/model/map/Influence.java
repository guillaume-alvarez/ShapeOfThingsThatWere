package com.galvarez.ttw.model.map;

import static java.lang.Math.max;

import java.util.Iterator;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntIntMap.Entry;

public final class Influence implements Iterable<IntIntMap.Entry> {

  public final MapPosition position;

  public final Terrain terrain;

  /** Entity id to influence score. */
  private final IntIntMap influence = new IntIntMap();

  /**
   * Entity id to influence delta from last turn. Only for display when starting
   * a new turn, player needs to know how things evolve.
   */
  private final IntIntMap influenceDelta = new IntIntMap();

  /**
   * Entity id to influence target for next turn. Used to compute the increment
   * at each turn.
   */
  private final IntIntMap influenceTarget = new IntIntMap();

  private int mainInfluence = 0;

  private int mainInfluenceSource = -1;

  private int secondInfluenceDiff = 0;

  public Influence(MapPosition position, Terrain terrain) {
    this.position = position;
    this.terrain = terrain;
  }

  /** Set the source influence to the tile. */
  public void setInfluence(Entity source, int inf) {
    influence.put(source.getId(), inf);
    recomputeMain();
  }

  /** Move the source influence on the tile. */
  public void moveInfluence(Entity from, Entity to) {
    influence.getAndIncrement(to.getId(), 0, influence.remove(from.getId(), 0));
    recomputeMain();
  }

  /** Remove the source influence from the tile. */
  public void removeInfluence(Entity source) {
    influence.remove(source.getId(), 0);
    influenceDelta.remove(source.getId(), 0);
    recomputeMain();
  }

  public Iterable<IntIntMap.Entry> getDelta() {
    return influenceDelta;
  }

  public int getDelta(Entity source) {
    return influenceDelta.get(source.getId(), 0);
  }

  public void clearInfluenceTarget() {
    influenceTarget.clear();
  }

  public void increaseTarget(Entity source, int target) {
    influenceTarget.getAndIncrement(source.getId(), 0, target);
  }

  /** Make each participant influence closer to target and compute delta. */
  public void computeNewInfluence() {
    influenceDelta.clear();

    // make sure all involved empires have a target influence
    for (Entry e : influence) {
      if (!influenceTarget.containsKey(e.key))
        influenceTarget.put(e.key, 0);
    }

    // then compute the delta and apply it
    for (Entry e : influenceTarget) {
      final int current = influence.get(e.key, 0);
      final int target = e.value;
      int delta = 0;
      if (target > current)
        delta = max(1, (target - current) / 10);
      else if (target < current)
        delta = -max(1, (current - target) / 10);

      if (delta != 0) {
        // do nothing if same obviously
        influence.put(e.key, max(0, current + delta));
        influenceDelta.put(e.key, delta);
      }
    }
    recomputeMain();
  }

  private void recomputeMain() {
    int secondInfluence = Integer.MIN_VALUE;
    mainInfluence = terrain.moveCost() - 1;
    mainInfluenceSource = -1;
    if (influence.size > 0) {
      for (Iterator<Entry> it = influence.iterator(); it.hasNext();) {
        Entry e = it.next();
        if (e.value > mainInfluence) {
          secondInfluence = mainInfluence;
          mainInfluenceSource = e.key;
          mainInfluence = e.value;
        } else if (e.value <= 0) {
          it.remove();
        } else if (e.value > secondInfluence) {
          secondInfluence = e.value;
        }
      }
    }
    if (secondInfluence > 0)
      secondInfluenceDiff = mainInfluence - secondInfluence;
  }

  public int getInfluence(Entity e) {
    return influence.get(e.getId(), 0);
  }

  public int getSecondInfluenceDiff() {
    return secondInfluenceDiff;
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
  public int hashCode() {
    return position.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    else
      return (obj instanceof Influence) && position.equals(((Influence) obj).position);
  }

  @Override
  public String toString() {
    return terrain + influence.toString();
  }

}
