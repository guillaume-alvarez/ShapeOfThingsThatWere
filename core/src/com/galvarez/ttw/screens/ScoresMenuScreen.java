package com.galvarez.ttw.screens;

import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.ScoreSystem;
import com.galvarez.ttw.model.ScoreSystem.Item;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * This screen appears when user tries to pause or escape from the main game
 * screen.
 * 
 * @author Guillaume Alvarez
 */
public final class ScoresMenuScreen extends AbstractPausedScreen<OverworldScreen> {

  private final FramedMenu topMenu, ladderMenu;

  private final ScoreSystem scoreSystem;

  public ScoresMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen,
      ScoreSystem scoreSystem) {
    super(game, world, batch, gameScreen);
    this.scoreSystem = scoreSystem;

    topMenu = new FramedMenu(skin, 800, 600);
    ladderMenu = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    topMenu.clear();
    topMenu.addButton("Resume game", this::resumeGame);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    ladderMenu.clear();
    ladderMenu
        .addLabel("One point per influenced tile, \n half the score from tributary, \n a quarter of the score from allies.");
    ladderMenu.addLabel(" --- ");

    int rank = 1;
    for (Item i : scoreSystem.getScores()) {
      ladderMenu.addLabel("No" + rank++ + " " + i.empire.getComponent(Description.class).desc + ": "
          + i.score.totalScore + " (+" + i.score.lastTurnPoints + ")");
    }

    ladderMenu.addToStage(stage, 30, topMenu.getY() - 30, true);
  }
}
