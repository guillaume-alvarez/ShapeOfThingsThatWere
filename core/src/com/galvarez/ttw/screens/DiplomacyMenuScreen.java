package com.galvarez.ttw.screens;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

import java.util.List;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * This screen appears when user selects the diplomacy menu. It displays
 * relations with neighbors and possible actions.
 * 
 * @author Guillaume Alvarez
 */
public final class DiplomacyMenuScreen extends AbstractPausedScreen<AbstractScreen> {

  private final FramedMenu topMenu;

  private final List<Empire> empires;

  public DiplomacyMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch, AbstractScreen gameScreen,
      List<Empire> empires) {
    super(game, world, batch, gameScreen);
    this.empires = empires;

    topMenu = new FramedMenu(skin, 800, 600);
  }

  @Override
  protected void initMenu() {
    topMenu.clear();
    topMenu.addButton("Resume game", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        resumeGame();
      }
    }, true);
    topMenu.addToStage(stage, 30, stage.getHeight() - 30, false);

    int maxWidth = 128;
    float centerX = (Gdx.graphics.getWidth() - maxWidth) * 0.5f;
    float centerY = topMenu.getY() * 0.5f;
    float radiusX = Gdx.graphics.getWidth() * 0.4f;
    float radiusY = topMenu.getY() * 0.4f;
    float angle = 2f * PI / empires.size();
    float angle1 = 0f;
    for (Empire e : empires) {
      FramedMenu menu = new FramedMenu(skin, maxWidth, 36);
      menu.addLabel(e.name());
      menu.addToStage(stage, centerX + radiusX * cos(angle1), centerY + radiusY * sin(angle1), false);
      angle1 += angle;
    }
  }
}
