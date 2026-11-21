package com.galvarez.ttw.screens;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.DiplomaticSystem;
import com.galvarez.ttw.model.DiplomaticSystem.Action;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * This screen appears when user selects the diplomacy menu. It displays
 * relations with neighbors and possible actions.
 * 
 * @author Guillaume Alvarez
 */
public final class DiplomacyMenuScreen extends AbstractPausedScreen<AbstractScreen> {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DiplomacyMenuScreen.class);

  private final FramedMenu topMenu;

  private FramedMenu actionMenu;

  private final List<Entity> empires;

  private final Entity player;

  private final DiplomaticSystem diplomaticSystem;

  private final List<FramedMenu> menus = new ArrayList<>();

  /** Descriptions shown in the action menu for each diplomatic action. */
  private static final String[] actionDescriptions = {
    "",  // NO_CHANGE
    "Army power on contested tiles; cities may be conquered.",  // DECLARE_WAR
    "Ends the war; normal influence resumes.",  // MAKE_PEACE
    "No influence or movement on each other's tiles. They get 25% of your score.",  // SIGN_TREATY
    "They receive your growth and 50% of your score."   // SURRENDER
  };

  /** Descriptions of the gameplay effects for each diplomatic state. */
  private static final String[] stateEffectDescriptions = {
    "No special effects",  // NONE
    "Army power on contested tiles. City conquest risk.",  // WAR
    "No influence or movement on their tiles. They get 25% of your score.",  // TREATY
    "No influence on their tiles. They spread your influence.",  // PROTECTORATE
    "They receive your growth and 50% of your score."   // TRIBUTE
  };

  public DiplomacyMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, AbstractScreen gameScreen,
      List<Entity> empires, Entity player, DiplomaticSystem diplomaticSystem) {
    super(game, world, batch, gameScreen);
    this.empires = empires;
    this.player = player;
    this.diplomaticSystem = diplomaticSystem;

    topMenu = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    if (actionMenu != null)
      actionMenu.clear();
    topMenu.clear();
    topMenu.addButton("Resume game", this::resumeGame);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    menus.forEach(m -> m.clear());
    menus.clear();

    int maxWidth = 200;
    float centerX = (Gdx.graphics.getWidth() - maxWidth) * 0.5f;
    float centerY = topMenu.getY() * 0.5f;
    float radiusX = Gdx.graphics.getWidth() * 0.38f;
    float radiusY = topMenu.getY() * 0.38f;
    float angle = 2f * PI / (player != null ? empires.size() - 1 : empires.size());
    float angle1 = 0f;
    Diplomacy playerDiplo = player != null ? player.getComponent(Diplomacy.class) : null;
    for (Entity empire : empires) {
      if (empire != player) {
        FramedMenu menu = new FramedMenu(skin, maxWidth, 200);
        menu.addLabel(empire.getComponent(Name.class).name);
        if (playerDiplo != null) {
          State relation = playerDiplo.getRelationWith(empire);
          menu.addButton(relation.toString(),
              () -> displayActionsMenu(menu, playerDiplo, empire));

          // Add score bar
          int score = playerDiplo.getScore(empire);
          menu.addScoreBar(score);

          // Add effect description (wrapped to fit, smaller font)
          menu.addSmallLabel(stateEffectDescriptions[relation.ordinal()]);
        }
        menu.addToStage(stage, centerX + radiusX * cos(angle1), centerY + radiusY * sin(angle1), false);
        menus.add(menu);
        angle1 += angle;
      }
    }

    // draw player at the center of the screen, so that it is easier for him to
    // understand the screen contents
    FramedMenu menu = new FramedMenu(skin, maxWidth, 36);
    menu.addLabel(player.getComponent(Name.class).name);
    menu.addToStage(stage, centerX, centerY, false);
    menus.add(menu);
  }

  private void displayActionsMenu(FramedMenu parent, Diplomacy diplo, Entity target) {
    if (actionMenu != null)
      actionMenu.clear();
    actionMenu = new FramedMenu(skin, 220, 300, parent);
    boolean hasActions = false;
    for (Action action : diplomaticSystem.getPossibleActions(diplo, target)) {
      hasActions = true;
      String desc = actionDescriptions[action.ordinal()];
      actionMenu.addButton(action.str, desc, new Runnable() {
        @Override
        public void run() {
          if (action != Action.NO_CHANGE)
            diplo.proposals.put(target, action);
          actionMenu.clear();
          initMenu();
        }
      }, true);
    }
    if (!hasActions)
      actionMenu.addLabel("No possible actions!");

    // Position the action menu to the right of the parent, centered vertically
    float actionX = parent.getX() + parent.getWidth() + 5;
    float actionY = parent.getY() + parent.getHeight() / 2f;

    // If it would go off the right edge, show it on the left instead
    if (actionX + actionMenu.getTable().getPrefWidth() > stage.getWidth()) {
      actionX = parent.getX() - actionMenu.getTable().getPrefWidth() - 5;
    }
    // If it would go off the top edge, adjust down
    if (actionY - 10 < 0) {
      actionY = actionMenu.getTable().getPrefHeight() / 2f + 10;
    }
    actionMenu.addToStage(stage, actionX, actionY, true);
  }
}
