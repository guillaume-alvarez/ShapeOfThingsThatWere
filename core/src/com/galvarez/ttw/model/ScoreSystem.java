package com.galvarez.ttw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Score;

/**
 * For every empire, compute score every turn.
 * <p>
 * Every turn get one point per influenced tile, half the turn score from
 * tributary and a quarter of the turn score from allies.
 * </p>
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class ScoreSystem extends EntitySystem {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ScoreSystem.class);

  private ComponentMapper<Score> scores;

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Diplomacy> relations;

  private final List<Item> list = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public ScoreSystem() {
    super(Aspect.getAspectForAll(Score.class, InfluenceSource.class));
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    list.add(new Item(e, scores.get(e)));
  }

  @Override
  protected void removed(Entity e) {
    super.removed(e);

    for (Iterator<Item> it = list.iterator(); it.hasNext();)
      if (it.next().empire.equals(e)) {
        it.remove();
        return;
      }
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity empire : entities) {
      Score score = scores.get(empire);
      score.lastTurnPoints = 0;
    }

    for (Entity empire : entities) {
      InfluenceSource source = sources.get(empire);
      add(empire, source.influencedTiles.size());
    }

    Collections.sort(list, Comparator.comparingInt((Item i) -> i.score.totalScore).reversed());
    for (int r = 0; r < list.size(); r++)
      list.get(r).score.rank = r + 1;
  }

  /** Recursive method to add score to all. */
  private void add(Entity empire, int delta) {
    if (delta > 0) {
      Diplomacy diplomacy = relations.get(empire);

      Score score = scores.get(empire);
      score.lastTurnPoints += delta;
      score.totalScore += delta;

      // add to overlords and allies
      for (Entry<Entity, State> e : diplomacy.relations.entrySet()) {
        if (e.getValue() == State.TREATY)
          add(e.getKey(), delta / 4);
        else if (e.getValue() == State.TRIBUTE)
          add(e.getKey(), delta / 2);
      }
    }
  }

  public final class Item {

    public final Entity empire;

    public final Score score;

    Item(Entity empire, Score score) {
      this.empire = empire;
      this.score = score;
    }

  }

  public List<Item> getScores() {
    return list;
  }

}
