package com.galvarez.ttw.screens;

import static java.lang.String.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.EffectsSystem;
import com.galvarez.ttw.model.PoliciesSystem;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Policy;
import com.galvarez.ttw.rendering.ui.FramedMenu;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * This screen appears when user tries to pause or escape from the main game
 * screen.
 * 
 * @author Guillaume Alvarez
 */
public final class PoliciesMenuScreen extends AbstractPausedScreen<OverworldScreen> {

  private final FramedMenu topMenu, empirePolicies;

  private final Policies policies;

  private final PoliciesSystem policiesSystem;

  private final Entity empire;

  private final EffectsSystem effects;

  public PoliciesMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen,
      Entity empire, PoliciesSystem policiesSystem, EffectsSystem effects) {
    super(game, world, batch, gameScreen);
    this.empire = empire;
    this.effects = effects;
    this.policies = empire.getComponent(Policies.class);
    this.policiesSystem = policiesSystem;

    topMenu = new FramedMenu(skin, 800, 600);
    empirePolicies = new FramedMenu(skin, 800, 600);
  }

  public final class Item {

    public final Discovery discovery;

    public Item(Discovery discovery) {
      this.discovery = discovery;
    }

    @Override
    public String toString() {
      return discovery.name + " ("
          + (discovery.effects.isEmpty() ? "no effect" : join(", ", effects.toString(discovery.effects)))//
          + ")";
    }

  }

  public final Item NONE = new Item(new Discovery("NONE", new ArrayList<String>()));

  @Override
  protected void initMenu() {
    topMenu.clear();
    topMenu.addButton("Resume game", () -> resumeGame());
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    empirePolicies.clear();
    for (Policy choice : Policy.values()) {
      empirePolicies.addLabel(choice.msg);

      Map<Discovery, Item> items = new HashMap<Discovery, Item>();
      for (Discovery d : policiesSystem.getAvailablePolicies(empire, choice))
        items.put(d, new Item(d));
      if (items.isEmpty()) {
        empirePolicies.addLabel("  No policy available");
      } else {
        Item selected = NONE;
        if (policies.policies.containsKey(choice)) {
          selected = items.get(policies.policies.get(choice));
        } else {
          items.put(NONE.discovery, NONE);
          selected = NONE;
        }
        empirePolicies.addSelectBox("  ", selected, items.values().toArray(new Item[items.size()]),
            i -> policiesSystem.applyPolicy(empire, choice, i != NONE ? i.discovery : null));
      }
    }
    empirePolicies.addToStage(stage, 30, topMenu.getY() - 30, false);
  }

}
