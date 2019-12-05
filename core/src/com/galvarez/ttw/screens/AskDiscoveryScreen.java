package com.galvarez.ttw.screens;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.DiscoverySystem;
import com.galvarez.ttw.model.Faction;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

import java.util.Map.Entry;

/**
 * This screen appears when player must choose his next discovery.
 * 
 * @author Guillaume Alvarez
 */
public final class AskDiscoveryScreen extends AbstractPausedScreen<OverworldScreen> {

  private final FramedMenu choices;

  private final Discoveries empire;

  private final DiscoverySystem discoverySystem;

  private final Entity player;

  public AskDiscoveryScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen,
      Entity player, DiscoverySystem discoverySystem) {
    super(game, world, batch, gameScreen);
    this.player = player;
    this.empire = player.getComponent(Discoveries.class);
    this.discoverySystem = discoverySystem;

    choices = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    choices.clear();

    if (empire.nextPossible == null || empire.nextPossible.isEmpty()) {
      // may be displayed from discovery menu
      choices.addButton("No discoveries to combine!", this::resumeGame);
    } else {
      choices.addLabel("Which faction do you choose to make new discoveries?");
      for (Entry<Faction, Research> next : empire.nextPossible.entrySet()) {
        String label = action(next.getKey()) + previousString(next.getValue());
        choices.addButton(label, () -> newDiscovery(label, next.getValue()));
      }

      choices.addLabel(" ");
      choices.addButton("Choose later...", this::resumeGame);
    }

    choices.addToStage(stage, -1, -1, true);
  }

  private static String previousString(Research next) {
    if (next.previous.isEmpty())
      return "our environment";

    StringBuilder sb = new StringBuilder();
    for (Discovery previous : next.previous)
      sb.append(previous.name).append(", ");
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

  private static String action(Faction faction) {
    switch (faction) {
      case CULTURAL:
        return "Cultural faction advises to meditate on ";
      case ECONOMIC:
        return "Economic faction suggest we pursue profit in ";
      case MILITARY:
        return "Military faction commands us to investigate ";
      default:
        throw new IllegalStateException("Unknown faction " + faction);
    }
  }

  private void newDiscovery(String label, Research next) {
    discoverySystem.discoverNew(player, empire, next);

    choices.clear();

    StringBuilder sb = new StringBuilder("We discovered " + next.target.name + "!");
    for (String effect : discoverySystem.effectsStrings(empire.last.target))
      sb.append("\n - " + effect);
    choices.addLabel(label + "...");
    choices.addButton(sb.toString(), this::resumeGame);

    choices.addToStage(stage, -1, -1, true);
  }
}
