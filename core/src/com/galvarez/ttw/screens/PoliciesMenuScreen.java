package com.galvarez.ttw.screens;

import static java.lang.Math.min;
import static java.lang.String.join;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.EffectsSystem;
import com.galvarez.ttw.model.PoliciesSystem;
import com.galvarez.ttw.model.RevoltSystem;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.InfluenceSource;
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

  private final FramedMenu topMenu, stabilityMenu, policiesMenu;

  private final Policies policies;

  private final PoliciesSystem policiesSystem;

  private final Entity empire;

  private final EffectsSystem effects;

  private final RevoltSystem revolts;

  public PoliciesMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen,
      Entity empire, PoliciesSystem policiesSystem, EffectsSystem effects, RevoltSystem revolts) {
    super(game, world, batch, gameScreen);
    this.empire = empire;
    this.effects = effects;
    this.revolts = revolts;
    this.policies = empire.getComponent(Policies.class);
    this.policiesSystem = policiesSystem;

    topMenu = new FramedMenu(skin, 800, 600);
    stabilityMenu = new FramedMenu(skin, 800, 600);
    policiesMenu = new FramedMenu(skin, 800, 600);
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

  public final Item NONE = new Item(new Discovery("NONE"));

  @Override
  protected void initMenu() {
    topMenu.clear();
    topMenu.addButton("Resume game", this::resumeGame);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    stabilityMenu.clear();
    InfluenceSource source = empire.getComponent(InfluenceSource.class);
    ArmyCommand army = empire.getComponent(ArmyCommand.class);
    stabilityMenu.addTable( //
        new Object[] { "Empire stability is", stabilityText(policies) }, //
        new Object[] { " + military power ", militaryText(army) }, //
        new Object[] { " - source extension ", extensionText(source) }, //
        new Object[] { "=> current instability risk is ", revolts.getInstabilityPercent(empire) + "%" });
    stabilityMenu.addToStage(stage, 30, topMenu.getY() - 30, false);

    policiesMenu.clear();
    for (Policy choice : Policy.values()) {
      policiesMenu.addLabel(choice.msg);

      Map<Discovery, Item> items = new HashMap<Discovery, Item>();
      for (Discovery d : policiesSystem.getAvailablePolicies(empire, choice))
        items.put(d, new Item(d));
      if (items.isEmpty()) {
        policiesMenu.addLabel("  No policy available");
      } else {
        Item selected = NONE;
        if (policies.policies.containsKey(choice)) {
          selected = items.get(policies.policies.get(choice));
        } else {
          items.put(NONE.discovery, NONE);
          selected = NONE;
        }
        policiesMenu.addSelectBox("  ", selected, items.values().toArray(new Item[items.size()]),
            i -> policiesSystem.applyPolicy(empire, choice, i != NONE ? i.discovery : null));
      }
    }
    policiesMenu.addToStage(stage, 30, stabilityMenu.getY() - 30, false);
  }

  private static String militaryText(ArmyCommand army) {
    int power = army.militaryPower;
    if (power >= 0)
      return "" + power;
    else
      return power + " (count only if positive)";
  }

  private static String extensionText(InfluenceSource source) {
    return source.influencedTiles.size() + " (influenced tiles)";
  }

  private static String stabilityText(Policies policies) {
    final int max = policies.stabilityMax;
    final int stability = policies.stability;

    StringBuilder sb = new StringBuilder();
    sb.append(stability);

    if (stability < max) {
      sb.append(" (+").append(min(policies.stabilityGrowth, max - stability)).append(" per turn)");
    } else if (stability > max) {
      sb.append(" (-1 per turn)");
    }

    return sb.toString();
  }
}
