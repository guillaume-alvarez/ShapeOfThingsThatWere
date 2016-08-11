package com.galvarez.ttw.screens;

import java.util.Map;
import java.util.Map.Entry;

import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * This screen appears when user tries to pause or escape from the main game
 * screen.
 * 
 * @author Guillaume Alvarez
 */
public final class AskEventScreen extends AbstractPausedScreen<OverworldScreen> {

  private final FramedMenu menu;

  private String label;

  private Map<String, Runnable> choices;

  public AskEventScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen) {
    super(game, world, batch, gameScreen);

    menu = new FramedMenu(skin, 800, 600);
  }

  public void setEvent(String label, Map<String, Runnable> choices) {
    this.label = label;
    this.choices = choices;
  }

  @Override
  protected void initMenu() {
    menu.clear();

    menu.addLabel(label);
    for (Entry<String, Runnable> choice : choices.entrySet())
      menu.addButton(choice.getKey(), "", () -> {
        choice.getValue().run();
        gameScreen.updateRendering();
        resumeGame();
      }, true);

    menu.addLabel(" ");
    menu.addButton("Choose later...", this::resumeGame);

    menu.addToStage(stage, -1, -1, true);
  }

}
