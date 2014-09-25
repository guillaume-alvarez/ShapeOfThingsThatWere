package com.galvarez.ttw.screens;

import static java.util.stream.Collectors.toList;

import java.util.ListIterator;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.data.Culture;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.rendering.ui.FramedMenu;

/**
 * This screen presents the user with a menu to launch the game.
 * 
 * @author Guillaume Alvarez
 */
public final class CustomizeGameMenuScreen extends AbstractScreen {

  private final Stage stage;

  private final SessionSettings settings;

  private final FramedMenu map;

  private final FramedMenu empires;

  private final FramedMenu actions;

  private final Texture square;

  public CustomizeGameMenuScreen(ThingsThatWereGame game, World world, SpriteBatch batch) {
    super(game, world, batch);
    this.settings = new SessionSettings();

    stage = new Stage(new ScreenViewport(), batch);

    Skin skin = new Skin(Gdx.files.internal("resources/uiskin/uiskin.json"));
    map = new FramedMenu(skin, 800, 160).nbColumns(2);
    empires = new FramedMenu(skin, 800, 480).nbColumns(3);
    actions = new FramedMenu(skin, 800, 160);

    square = createSquare();

    updateMenu();
  }

  private Texture createSquare() {
    // A Pixmap is basically a raw image in memory as repesented by pixels
    // We create one 16 wide, 16 height using 8 bytes for Red, Green, Blue and
    // Alpha channels
    Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);

    // Fill it red
    pixmap.setColor(Color.WHITE);
    pixmap.fill();

    return new Texture(pixmap);
  }

  private void updateMenu() {
    map.clear();
    map.addTextField("noise", settings.mapNoise, (textField, c) -> settings.mapNoise.set(textField.getText()));
    map.addTextField("height", settings.mapHeight, (textField, c) -> settings.mapHeight.set(textField.getText()));
    map.addTextField("width", settings.mapWidth, (textField, c) -> settings.mapWidth.set(textField.getText()));
    map.addToStage(stage, 30, stage.getHeight() - 30, false);

    empires.clear();
    LabelStyle style = new LabelStyle(empires.getSkin().get(LabelStyle.class));
    empires.getTable().add(new Label("EMPIRE", style));
    empires.getTable().add(new Label("CULTURE", style));
    empires.getTable().add(new Label("CONTROL", style));
    empires.getTable().row();
    for (Empire e : settings.empires)
      displayEmpire(e, empires.getSkin(), empires.getTable());
    empires.addToStage(stage, 30, map.getY() - 30, false);
    stage.setScrollFocus(empires.getTable());

    actions.clear();
    actions.addButton("Add new empire", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        createNewEmpire();
      }
    }, settings.empires.size() < SessionSettings.COLORS.size());
    actions.addButton("Start new game", new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        game.startGame(settings);
      }
    }, true);
    actions.addToStage(stage, 30, empires.getY() - 30, false);
  }

  private void displayEmpire(Empire empire, Skin skin, Table table) {
    Image i = new Image(square);
    i.setColor(empire.color);
    table.add(i).minHeight(i.getMinHeight()).prefHeight(i.getPrefHeight());

    SelectBox<Culture> sb = new SelectBox<Culture>(skin.get(SelectBoxStyle.class));
    sb.setItems(settings
        .getCultures()
        .stream()
        .filter(
            c -> c == empire.culture
                || settings.empires.stream().filter(e -> e.getCulture() == c).count() < c.cities.size)
        .collect(toList()).toArray(new Culture[0]));
    sb.setSelected(empire.culture);
    sb.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        changeCulture(empire, sb.getSelected());
      }
    });
    table.add(sb).minHeight(sb.getMinHeight()).prefHeight(sb.getMinHeight());

    Button b = new Button(skin.get(ButtonStyle.class));
    LabelStyle style = new LabelStyle(skin.get(LabelStyle.class));
    style.fontColor = empire.color;
    b.add(new Label(empire.isComputerControlled() ? "CPU" : "Player", style));
    b.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        changeController(empire);
      }
    });
    table.add(b).minHeight(b.getMinHeight()).prefHeight(b.getPrefHeight());

    table.row();
  }

  private void changeCulture(Empire e, Culture selected) {
    settings.empires.set(settings.empires.indexOf(e), new Empire(e.color, selected, e.isComputerControlled()));
    updateMenu();
  }

  private void changeController(Empire e) {
    if (e.isComputerControlled()) {
      // can only have 1 player-controlled empire
      for (ListIterator<Empire> it = settings.empires.listIterator(); it.hasNext();) {
        Empire empire = it.next();
        it.set(new Empire(e.color, e.culture, empire != e));
      }
    } else
      settings.empires.set(settings.empires.indexOf(e), new Empire(e.color, e.culture, true));
    updateMenu();
  }

  /**
   * Create a new empire from random data. It tries to use unused
   * culture/colors.
   */
  private void createNewEmpire() {
    settings.empires.add(new Empire(settings.guessColor(), settings.guessCulture(), true));
    updateMenu();
  }

  @Override
  public void show() {
    super.show();

    Gdx.input.setInputProcessor(stage);
  }

  @Override
  public void render(float delta) {
    super.render(delta);

    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);

    stage.getViewport().update(width, height, true);
    /*
     * The GUI use a ScreenViewport, meaning it won't scale when screen size
     * change. This is fine because we don't want the GUI size to change,
     * becoming zoomed in and ugly or zoomed and unreadable. However it has a
     * small side effect: the existing menu were placed according to the
     * vertical stage size. The stage size changed with the screen (game window)
     * one. So we must recompute the GUI elements coordinates. The simlest way
     * to do it is to recreate the menu.
     */
    updateMenu();
  }

  @Override
  public void dispose() {
    super.dispose();

    stage.dispose();
  }

}
