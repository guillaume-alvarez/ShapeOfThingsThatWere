package com.galvarez.ttw.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.galvarez.ttw.ThingsThatWereGame;
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

  public PoliciesMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, OverworldScreen gameScreen,
      Entity empire, PoliciesSystem policiesSystem) {
    super(game, world, batch, gameScreen);
    this.empire = empire;
    this.policies = empire.getComponent(Policies.class);
    this.policiesSystem = policiesSystem;

    topMenu = new FramedMenu(skin, 800, 600);
    empirePolicies = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    topMenu.clear();
    topMenu.addButton("Resume game", () -> resumeGame());
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    empirePolicies.clear();
    for (Policy choice : Policy.values()) {
      Label l = new Label(choice.msg, skin.get(LabelStyle.class));
      empirePolicies.getTable().add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

      SelectBox<Item> sb = new SelectBox<Item>(skin.get(SelectBoxStyle.class));

      Map<Discovery, Item> items = new HashMap<Discovery, Item>();
      for (Discovery d : policiesSystem.getAvailablePolicies(empire, choice))
        items.put(d, new Item(d));

      if (policies.policies.containsKey(choice)) {
        sb.setItems(items.values().toArray(new Item[items.size()]));
        sb.setSelected(items.get(policies.policies.get(choice)));
      } else {
        items.put(NONE.discovery, NONE);
        sb.setItems(items.values().toArray(new Item[items.size()]));
        sb.setSelected(NONE);
      }
      sb.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (sb.getSelected() != NONE)
            policiesSystem.applyPolicy(empire, choice, sb.getSelected().discovery);
        }
      });
      empirePolicies.getTable().add(sb).minHeight(sb.getMinHeight()).prefHeight(sb.getMinHeight());

      empirePolicies.getTable().row();
    }
    empirePolicies.addToStage(stage, 30, topMenu.getY() - 30, false);
  }

  private static final class Item {

    private final Discovery discovery;

    public Item(Discovery discovery) {
      this.discovery = discovery;
    }

    @Override
    public String toString() {
      if (discovery.effects.isEmpty())
        return discovery.name + " (no effect)";
      else
        return discovery.name + " " + discovery.effects;
    }

  }

  private static final Item NONE = new Item(new Discovery("NONE", new ArrayList<String>()));

}
