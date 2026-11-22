package com.galvarez.ttw.rendering.ui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static java.lang.Math.max;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class FramedDialog {

  private final VisDialog dialog;

  private final VisImage frame;

  public FramedDialog(Skin skin, String title, String message) {
    frame = new VisImage(skin.getPatch("frame"));

    dialog = new VisDialog(title) {
      @Override
      protected void result(Object object) {
        fadeOut();
      }
    };
    dialog.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable(VisUI.getSkin().getRegion("big-marble-texture")));
    dialog.getContentTable().defaults().expandX().fillX();

    VisLabel label = new VisLabel(message, VisUI.getSkin().get(com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle.class));
    label.setAlignment(Align.center);
    label.setWrap(true);

    dialog.text(label);
  }

  public void setKey(int key, Object result) {
    dialog.key(key, result);
  }

  public void addButton(String text, Runnable listener) {
    VisTextButton button = new VisTextButton(text,
        VisUI.getSkin().get(VisTextButton.VisTextButtonStyle.class));

    button.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (listener != null)
          listener.run();
        fadeOut();
      }
    });
    dialog.button(button);
  }

  public void addButton(Button button) {
    button.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        fadeOut();
      }
    });
    dialog.button(button);
  }

  public void addToStage(Stage stage, int minWidth, int minHeight) {
    stage.addActor(dialog);
    stage.addActor(frame);

    int width = max(minWidth, (int) dialog.getPrefWidth());
    int height = max(minHeight, (int) dialog.getPrefHeight());
    dialog.setWidth(width);
    dialog.setHeight(height);
    int x = (int) (stage.getWidth() / 2 - width / 2);
    int y = (int) (stage.getHeight() / 2 - height / 2);
    dialog.setX(x);
    dialog.setY(y);
    frame.setX(x - 1);
    frame.setY(y - 3);
    frame.setWidth(width + 4);
    frame.setHeight(height + 4);

    frame.setTouchable(Touchable.disabled);
  }

  public void fadeOut() {
    frame.addAction(sequence(Actions.fadeOut(0.5f, Interpolation.fade), Actions.removeActor()));
  }
}