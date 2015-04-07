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
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Score;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

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

  private ComponentMapper<Discoveries> discoveries;

  private Item winner;

  private final List<Item> list = new ArrayList<>();

  private final int nbDiscoveries;

  private final OverworldScreen screen;

  @SuppressWarnings("unchecked")
  public ScoreSystem(SessionSettings s, OverworldScreen screen) {
    super(Aspect.getAspectForAll(Score.class, InfluenceSource.class));
    this.screen = screen;

    nbDiscoveries = s.getDiscoveries().size();
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    Score score = scores.get(e);
    score.nbDiscoveriesMax = nbDiscoveries;
    list.add(new Item(e, score));
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
      // every empire controls itself
      score.nbControlled = 1;
    }
    int nbControlledMax = entities.size();

    // compute current score (may modify other entities' scores)
    for (Entity empire : entities) {
      InfluenceSource source = sources.get(empire);
      add(empire, source.influencedTiles.size(), 0);

      Score score = scores.get(empire);
      score.nbDiscoveries = discoveries.get(empire).done.size();
      score.nbControlledMax = nbControlledMax;
    }

    // now score is stable, sort by rank and search for winners
    Collections.sort(list, Comparator.comparingInt((Item i) -> i.score.totalScore).reversed());
    for (int r = 0; r < list.size(); r++) {
      Item i = list.get(r);
      i.score.rank = r + 1;
      if (winner == null && hasWon(i.score))
        winner = i;
    }

    if (winner != null)
      screen.scoresMenu();
  }

  private static boolean hasWon(Score score) {
    // also test "greater" in case other empires were destroyed
    // it should not really happen but it does not cost anything
    return score.nbControlled >= score.nbControlledMax || score.nbDiscoveries >= score.nbDiscoveriesMax;
  }

  /** Recursive method to add score to all overlords and partners. */
  private void add(Entity empire, int delta, int nbControlled) {
    if (delta > 0
    // do not forget an empire can be deleted
        && scores.has(empire)) {
      Diplomacy diplomacy = relations.get(empire);

      Score score = scores.get(empire);
      score.lastTurnPoints += delta;
      score.totalScore += delta;
      score.nbControlled += nbControlled;

      // add to overlords and allies
      for (Entry<Entity, State> e : diplomacy.relations.entrySet()) {
        if (e.getValue() == State.TREATY)
          add(e.getKey(), delta / 4, 0);
        else if (e.getValue() == State.TRIBUTE)
          // overlord controls me and my vassals
          add(e.getKey(), delta / 2, score.nbControlled + 1);
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

  /** Get the winner for current game. <code>null</code> if nobody has won yet. */
  public Item getWinner() {
    return winner;
  }

  public List<Item> getScores() {
    return list;
  }

}
