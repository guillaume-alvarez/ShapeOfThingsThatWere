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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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

  private static final Logger log = LoggerFactory.getLogger(DiplomacyMenuScreen.class);

  private final FramedMenu topMenu;

  private FramedMenu actionMenu;

  private final List<Entity> empires;

  private final Entity player;

  private final DiplomaticSystem diplomaticSystem;

  private final List<FramedMenu> menus = new ArrayList<>();

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
    topMenu.addButton("Resume game", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        resumeGame();
      }
    }, true);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    menus.forEach(m -> m.clear());
    menus.clear();

    int maxWidth = 128;
    float centerX = (Gdx.graphics.getWidth() - maxWidth) * 0.5f;
    float centerY = topMenu.getY() * 0.5f;
    float radiusX = Gdx.graphics.getWidth() * 0.4f;
    float radiusY = topMenu.getY() * 0.4f;
    float angle = 2f * PI / (player != null ? empires.size() - 1 : empires.size());
    float angle1 = 0f;
    Diplomacy diplo = player != null ? player.getComponent(Diplomacy.class) : null;
    for (Entity empire : empires) {
      if (empire != player) {
        FramedMenu menu = new FramedMenu(skin, maxWidth, 64);
        menu.addLabel(empire.getComponent(Name.class).name);
        if (diplo != null) {
          menu.addButton(diplo.getRelationWith(empire).toString(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              displayActionsMenu(menu, diplo, empire);
            }
          }, true);
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
    actionMenu = new FramedMenu(skin, 256, 128, parent);
    boolean hasActions = false;
    for (Action action : diplomaticSystem.getPossibleActions(diplo, target)) {
      hasActions = true;
      actionMenu.addButton(action.str, new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (action != Action.NO_CHANGE)
            diplo.proposals.put(target, action);
          actionMenu.clear();
        }
      }, true);
    }
    if (!hasActions)
      actionMenu.addLabel("No possible actions!");
    actionMenu.addToStage(stage, parent.getX() + parent.getWidth(), parent.getY() + 10, true);
  }
}
