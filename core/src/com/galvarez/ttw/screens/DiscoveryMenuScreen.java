package com.galvarez.ttw.screens;

import java.util.Map;
import java.util.Map.Entry;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.DiscoverySystem;
import com.galvarez.ttw.model.Faction;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * This screen appears when user tries to pause or escape from the main game
 * screen.
 * 
 * @author Guillaume Alvarez
 */
public final class DiscoveryMenuScreen extends AbstractPausedScreen<OverworldScreen> {

  private final FramedMenu topMenu, lastDiscovery, discoveryChoices;

  private final Discoveries empire;

  private final DiscoverySystem discoverySystem;

  private final Entity entity;

  public DiscoveryMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen,
      Entity empire, DiscoverySystem discoverySystem) {
    super(game, world, batch, gameScreen);
    this.entity = empire;
    this.empire = empire.getComponent(Discoveries.class);
    this.discoverySystem = discoverySystem;

    topMenu = new FramedMenu(skin, 800, 600);
    lastDiscovery = new FramedMenu(skin, 800, 600);
    discoveryChoices = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    topMenu.clear();
    topMenu.addButton("Resume game", this::resumeGame);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    lastDiscovery.clear();
    if (empire.last == null) {
      lastDiscovery.addLabel("- No last discovery -");
    } else {
      lastDiscovery.addLabel("- Last discovery (effect doubled): " + empire.last.target.name + " -");
      lastDiscovery.addLabel("Discovered from " + discoverySystem.previousString(empire.last));
      lastDiscovery.addLabel("Effects:");
      for (String effect : discoverySystem.effectsStrings(empire.last.target))
        lastDiscovery.addLabel(" - " + effect);
    }
    lastDiscovery.addToStage(stage, 30, topMenu.getY() - 30, false);

    discoveryChoices.clear();
    if (empire.next != null) {
      discoveryChoices.addLabel("Progress toward new discovery from " + discoverySystem.previousString(empire.next)
          + ": " + empire.next.progress + "%");
    } else {
      Map<Faction, Research> possible = discoverySystem.possibleDiscoveries(entity, empire);
      if (possible.isEmpty()) {
        discoveryChoices.addLabel("No discoveries to combine!");
      } else {
        discoveryChoices.addLabel("Choose discoveries to combine:");
        for (Entry<Faction, Research> next : possible.entrySet())
          discoveryChoices.addButton(
              next.getKey() + " faction advises: ",
              discoverySystem.previousString(next.getValue()) + " (~"
                  + discoverySystem.guessNbTurns(empire, entity, next.getValue().target) + " turns)", //
              new Runnable() {
                @Override
                public void run() {
                  empire.next = next.getValue();
                  resumeGame();
                }
              }, true);
      }
    }
    discoveryChoices.addToStage(stage, 30, lastDiscovery.getY() - 30, false);
  }
}
