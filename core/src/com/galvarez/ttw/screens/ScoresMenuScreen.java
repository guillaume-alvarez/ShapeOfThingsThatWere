package com.galvarez.ttw.screens;

import static com.galvarez.ttw.utils.Colors.markup;

import java.util.ArrayList;
import java.util.List;

import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
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
    ladderMenu.nbColumns(3).addLabel(
        "One point per influenced tile, \n\thalf the score from tributary, \n\ta quarter of the score from allies.");
    ladderMenu.addLabel(" --- ");

    int rank = 1;
    List<Object[]> ladder = new ArrayList<>();
    ladder.add(new Object[] { "[BLACK]Rank", "[BLACK]Empire", "[BLACK]Score (progress)" });
    for (Item i : scoreSystem.getScores())
      ladder.add(new Object[] { "[BLACK]" + rank++, markup(i.empire) + i.empire.getComponent(Description.class).desc,
          "[BLACK]" + i.score.totalScore + " (+" + i.score.lastTurnPoints + ")" });
    LabelStyle style = ladderMenu.getSkin().get("colored", LabelStyle.class);
    style.font.setMarkupEnabled(true);
    ladderMenu.addTable(style, ladder.toArray(new Object[0][]));

    ladderMenu.addToStage(stage, 30, topMenu.getY() - 30, true);
  }
}
