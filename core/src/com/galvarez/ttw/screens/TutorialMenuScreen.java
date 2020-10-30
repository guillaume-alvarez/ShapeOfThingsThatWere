package com.galvarez.ttw.screens;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.ScoreSystem.Item;
import com.galvarez.ttw.model.components.Score;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

import java.util.ArrayList;
import java.util.List;

import static com.galvarez.ttw.utils.Colors.markup;
import static java.lang.String.format;
import static java.lang.String.join;

/**
 * This screen appears when user starts a new game, showing basic information.
 *
 * @author Guillaume Alvarez
 */
public final class TutorialMenuScreen extends AbstractPausedScreen<OverworldScreen> {

  private final FramedMenu topMenu, tutorialMenu;

  public TutorialMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen) {
    super(game, world, batch, gameScreen);

    super.paused = false;

    topMenu = new FramedMenu(skin, 800, 600);
    tutorialMenu = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    topMenu.clear();
    topMenu.addButton("Play the game", this::resumeGame);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    tutorialMenu.clear();
    tutorialMenu.addTitle("The Shape of Things that Were", game.assets.getFont(32));
    Label l = tutorialMenu.addLabel(generateTutorialText(gameScreen.player), true);
    tutorialMenu.addToStage(stage, 30, topMenu.getY() - 30, true);
  }

  private static String generateTutorialText(Entity player) {
    return join("\n\n",
            format("The tribe of %s is wandering in the world."
                            + " It is time to lead it to civilization, discover and innovate, found and grow your city, threaten war and ask for tribute.",
                    player.getComponent(Name.class)),
            format("Your tribe is the seat of your power, from which your influence will spread, tile by tile."
                    + " Until you found a village or a city you can move around the world, finding the perfect spot for your civilization."
                    + " take care of the terrain you walk on as it will impact your growth and the extent of your influence."),
            format("You can discover new natural products, feats of technology or social organizations."
                    + " Each new discovery will slightly change your civilization and how it exploits the terrain in your influence."),
            format("Create armies and send them far from your seat of power."
                    + " Their power will increase your influence where you will place them, maybe allowing to take tiles from weaker neighbors.")
    );
  }

}
