package com.galvarez.ttw.rendering.ui;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

/**
 * Builds the game's generic menu content from VisUI widgets. Map-specific
 * placement and game actions remain in the callers.
 */
public class FramedMenu {

  private int nbColumns = 2;

  private VisScrollPane scrollPane;

  private VisTable table;

  private final com.badlogic.gdx.scenes.scene2d.ui.Skin skin;

  /** max's represent the largest we want the menu getting */
  private final float maxHeight;

  private float width;

  /** The parent menu is the one we will focus on if the user closes this one */
  private final FramedMenu parent;

  private static BitmapFont smallFont = null;

  private static BitmapFont getSmallFont() {
    if (smallFont == null) {
      FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Romanica.ttf"));
      FreeTypeFontParameter parameter = new FreeTypeFontParameter();
      parameter.size = 12;
      smallFont = generator.generateFont(parameter);
      generator.dispose();
    }
    return smallFont;
  }

  public FramedMenu(com.badlogic.gdx.scenes.scene2d.ui.Skin skin, float width, float maxHeight) {
    this(skin, width, maxHeight, null);
  }

  public FramedMenu(com.badlogic.gdx.scenes.scene2d.ui.Skin skin, float width, float maxHeight, FramedMenu parent) {
    this.skin = skin;
    this.table = new VisTable();
    table.defaults().expandX().fillX().left();

    this.maxHeight = maxHeight;
    this.width = width;
    this.parent = parent;
  }

  public FramedMenu nbColumns(int nb) {
    this.nbColumns = nb;
    return this;
  }

  /** Use only for specific menus. */
  public com.badlogic.gdx.scenes.scene2d.ui.Skin getSkin() {
    return skin;
  }

  /** Use only for specific menus. */
  public VisTable getTable() {
    return table;
  }

  public <E> void addSelectBox(String label, E selected, E[] values, Consumer<E> lis) {
    LabelStyle style = skin.get(LabelStyle.class);
    VisLabel l = new VisLabel(label, style);
    table.add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

    VisSelectBox<E> sb = new VisSelectBox<>(skin.get(com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle.class));
    sb.setItems(values);
    sb.setSelected(selected);
    sb.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        lis.accept(sb.getSelected());
      }
    });
    com.badlogic.gdx.scenes.scene2d.ui.Cell<VisSelectBox<E>> right = table.add(sb).minHeight(sb.getMinHeight()).prefHeight(sb.getPrefHeight());
    if (nbColumns > 2)
      right.colspan(nbColumns - 1);

    table.row();
  }

  public void addBooleanSelectBox(String label, boolean selected, Consumer<Boolean> lis) {
    LabelStyle style = skin.get(LabelStyle.class);
    VisLabel l = new VisLabel(label, style);
    table.add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

    VisSelectBox<Boolean> sb = new VisSelectBox<>(skin.get(com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle.class));
    sb.setItems(Boolean.TRUE, Boolean.FALSE);
    sb.setSelected(selected);
    sb.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        lis.accept(sb.getSelected());
      }
    });
    com.badlogic.gdx.scenes.scene2d.ui.Cell<VisSelectBox<Boolean>> right = table.add(sb).minHeight(sb.getMinHeight()).prefHeight(sb.getPrefHeight());
    if (nbColumns > 2)
      right.colspan(nbColumns - 1);

    table.row();
  }

  public void addTextField(String label, Object value, com.kotcrab.vis.ui.widget.VisTextField.TextFieldListener lis) {
    VisLabel l = new VisLabel(label, skin.get(LabelStyle.class));
    table.add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

    VisTextField t = new VisTextField(String.valueOf(value));
    t.setTextFieldListener(lis);
    t.setDisabled(false);
    t.setMaxLength(6);
    if (value instanceof Number)
      t.setTextFieldFilter((textField, c) -> Character.isDigit(c));
    table.add(t).right().padLeft(1f);

    table.row();
  }

  public VisTextButton addButton(String label, Runnable action) {
    return addButton(label, null, action, true);
  }

  /**
   * Adds a button to the menu, with a secondary label (like MP cost) aligned to
   * the right
   */
  public VisTextButton addButton(String label, String secondaryLabel, Runnable action, boolean active) {
    return addButton(active ? skin.get(LabelStyle.class) : skin.get("inactive", LabelStyle.class), label, secondaryLabel,
            action, active);
  }

  public VisTextButton addButton(LabelStyle style, String label, String secondaryLabel, Runnable action, boolean active) {
    VisTextButton b = new VisTextButton(label, skin.get(VisTextButton.VisTextButtonStyle.class));
    if (action != null)
      b.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          action.run();
        }
      });
    b.setDisabled(!active);

    // Use a table inside the button to stack label and description vertically
    VisTable buttonTable = new VisTable();
    VisLabel mainLabel = new VisLabel(label, style);
    mainLabel.setAlignment(Align.center);
    buttonTable.add(mainLabel).center();
    buttonTable.row();
    if (secondaryLabel != null && !secondaryLabel.isEmpty()) {
      LabelStyle descStyle = new LabelStyle(style);
      descStyle.font = getSmallFont();
      descStyle.fontColor = style.fontColor;
      VisLabel descLabel = new VisLabel(secondaryLabel, descStyle);
      descLabel.setWidth(width - 16);
      descLabel.setWrap(true);
      descLabel.setAlignment(Align.center);
      buttonTable.add(descLabel).center().width(width - 16);
    }
    b.clearChildren();
    b.add(buttonTable).fillX().left();

    table.add(b).left().colspan(nbColumns).width(width - 6);
    table.row();

    return b;
  }

  /**
   * Adds a button to the menu, with an icon on the left and label on the right.
   */
  public VisTextButton addButtonSprite(Drawable icon, String label, Runnable action, boolean active) {
    LabelStyle style = active ? skin.get(LabelStyle.class) : skin.get("inactive", LabelStyle.class);

    VisTextButton b = new VisTextButton(label, skin.get(VisTextButton.VisTextButtonStyle.class));
    if (action != null)
      b.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          action.run();
        }
      });
    b.setDisabled(!active);

    b.clearChildren();
    b.add(new VisImage(icon, Scaling.fit)).left();
    VisLabel l = new VisLabel(label, style);
    b.add(l).padLeft(8).right().expandX().fillX();

    table.add(b).left().colspan(nbColumns).minHeight(b.getMinHeight()).prefHeight(b.getPrefHeight());
    table.row();

    return b;
  }

  /** Adds a label followed by a sprite to the menu. */
  public VisLabel addLabelSprite(String label, TextureRegion region, Color color) {
    LabelStyle style = skin.get(LabelStyle.class);
    VisLabel l = new VisLabel(label, style);
    table.add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

    VisImage i = new VisImage(region);
    i.setColor(color);
    i.setScaling(Scaling.fit);
    com.badlogic.gdx.scenes.scene2d.ui.Cell<VisImage> right = table.add(i).minHeight(region.getRegionHeight()).prefHeight(region.getRegionHeight()).right();
    if (nbColumns > 2)
      right.colspan(nbColumns - 1);

    table.row();
    return l;
  }

  /** Adds a label to the menu. */
  public VisLabel addLabel(String label) {
    return addLabel(label, false);
  }

  /** Adds a label to the menu. */
  public VisLabel addLabel(String label, boolean wrap) {
    LabelStyle style = skin.get(LabelStyle.class);
    VisLabel l = new VisLabel(label, style);
    l.setWidth(width - 6);
    l.setWrap(wrap);

    table.add(l).left().colspan(nbColumns).width(width - 6).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());
    table.row();

    return l;
  }

  /** Adds a label with a smaller font (12px) to the menu, always wrapped. */
  public VisLabel addSmallLabel(String label) {
    LabelStyle style = new LabelStyle(skin.get(LabelStyle.class));
    style.font = getSmallFont();
    VisLabel l = new VisLabel(label, style);
    l.setWidth(width - 6);
    l.setWrap(true);

    table.add(l).left().colspan(nbColumns).width(width - 6);
    table.row();

    return l;
  }

  /** Add a title to the menu. */
  public VisLabel addTitle(String title, BitmapFont font) {
    LabelStyle style = new LabelStyle(skin.get(LabelStyle.class));
    style.font = font;

    VisLabel l = new VisLabel(title, style);
    l.setWidth(width);
    l.setAlignment(Align.center);

    table.add(l).left().colspan(nbColumns).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());
    table.row();

    return l;
  }

  /** Adds a label to the menu. */
  public void addTable(Object[]... data) {
    addTable(skin.get(LabelStyle.class), data);
  }

  /** Adds a label to the menu. */
  public void addTable(LabelStyle style, Object[]... data) {
    for (int row = 0; row < data.length; row++) {
      for (int col = 0; col < data[row].length; col++)
        table.add(new VisLabel(String.valueOf(data[row][col]), style)).left().padLeft(5f).padRight(15f);
      table.row();
    }
  }

  /**
   * Adds a label to the menu, using color markup from
   * https://github.com/libgdx/libgdx/wiki/Color-Markup-Language.
   */
  public VisLabel addColoredLabel(String label) {
    VisLabel l = new VisLabel(label, skin.get("colored", LabelStyle.class));

    table.add(l).left().colspan(nbColumns).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());
    table.row();

    return l;
  }

  /** Adds a sprite to the menu */
  public void addSprite(TextureRegion region) {
    VisImage i = new VisImage(region);
    i.setScaling(Scaling.fit);

    table.add(i).colspan(nbColumns).minHeight(region.getRegionHeight()).prefHeight(region.getRegionHeight());
    table.row();
  }

  public VisCheckBox addCheckBox(String text, boolean checked, Consumer<Boolean> lis) {
    VisCheckBox cb = new VisCheckBox(text, checked);
    if (lis != null)
      cb.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          lis.accept(Boolean.valueOf(cb.isChecked()));
        }
      });

    table.add(cb).colspan(nbColumns).minHeight(cb.getMinHeight()).prefHeight(cb.getPrefHeight());
    table.row();
    return cb;
  }

  public Actor buildMenu() {
    scrollPane = new VisScrollPane(table);
    scrollPane.setStyle(skin.get("default", com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle.class));

    TiledDrawable background = new TiledDrawable(skin.getRegion("big-marble-texture"));
    // the table use the background minimal size as its minimal size,
    // even though it is usually smaller than our big image
    background.setMinSize(0, 0);
    table.setBackground(background);
    table.pad(3, 1, 2, 1);

    // Calculate the actual preferred width from content, but cap at our max width
    float contentWidth = Math.min(table.getPrefWidth(), width);
    table.setWidth(contentWidth);

    // If the table does not fill our maximum size, resize it to our
    // estimated height, and disable scrolling both x and y
    if (table.getPrefHeight() < maxHeight) {
      scrollPane.setScrollingDisabled(true, true);
      scrollPane.setHeight(table.getPrefHeight());
    }

    // Otherwise, it's bigger than our maximum size, so we need to
    // enable vertical scrolling, and set the height to our max.
    else {
      scrollPane.setScrollingDisabled(true, false);
      scrollPane.setHeight(maxHeight);
    }

    // Set the scroll pane width to match the table width
    scrollPane.setWidth(contentWidth);

    // Move the table to the far left of the scrollPane
    table.left();

    // Prevent the scrollPane from scrolling (and snapping back) beyond the
    // scroll limits
    scrollPane.setOverscroll(false, false);
    scrollPane.setFillParent(false);

    return scrollPane;
  }

  /**
   * Adds the frame and scrollpane to the specified stage at the specified
   * location. Sizes the scrollpane to the (estimated) table size, up to a
   * maximum given in the constructor.
   * <p>
   * If x or y coordinate is negative, center the menu in the corresponding
   * dimension.
   *
   * @param canEscape if true pressing on ESC key will close the menu
   */
  public void addToStage(final Stage stage, float x, float y, boolean canEscape) {
    buildMenu();

    if (canEscape) {
      // If the user presses "ESC", close this menu and focus on the "parent"
      stage.setKeyboardFocus(scrollPane);
      scrollPane.addListener(new InputListener() {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
          if (keycode == Keys.ESCAPE) {
            // If this menu is invisible, don't do anything
            if (!scrollPane.isVisible())
              return false;

            // If there is a parent, get rid of this
            // menu and focus on it
            if (parent != null) {
              stage.setKeyboardFocus(parent.scrollPane);
              parent.enable();
              clear();
            }
            // Otherwise this must be the last one, so just clear it all
            else {
              stage.clear();
            }
          }
          return true;
        }
      });
    }

    // Go ahead and add them to the stage
    stage.addActor(scrollPane);

    // If y is negative, center the scrollPane vertically on the stage
    if (y < 0)
      scrollPane.setY((int) ((stage.getHeight() - scrollPane.getHeight()) / 2f));
    else
      scrollPane.setY(y - scrollPane.getHeight());
    // If x is negative, do likewise
    if (x < 0)
      scrollPane.setX((int) ((stage.getWidth() - scrollPane.getWidth()) / 2f));
    else
      scrollPane.setX(x);

    // In case they became invisible earlier, make them visible now
    scrollPane.setVisible(true);
  }

  public void setWidth(float width) {
    this.width = width;
  }

  /** Wipe all the buttons off, and remove widgets from stage. */
  public void clear() {
    table = new VisTable();
    table.defaults().expandX().fillX().left();
    if (scrollPane != null)
      scrollPane.remove();
  }

  public float getY() {
    return scrollPane.getY();
  }

  public float getX() {
    return scrollPane.getX();
  }

  public float getWidth() {
    return scrollPane.getWidth();
  }

  public float getHeight() {
    return scrollPane.getHeight();
  }

  /** Make it untouchable, and gray/transparent it out */
  public void disable() {
    scrollPane.setTouchable(Touchable.disabled);
    table.setColor(0.7f, 0.7f, 0.7f, 0.7f);
  }

  /** Re-enable */
  public void enable() {
    scrollPane.setTouchable(Touchable.enabled);
    table.setColor(1, 1, 1, 1);
  }

  /** Make invisible or visible */
  public void setVisible(boolean visible) {
    if (scrollPane != null)
      scrollPane.setVisible(visible);
  }

  /** Let someone else know who your parent is - currently used in MenuBuilder */
  public FramedMenu getParent() {
    return parent;
  }
}