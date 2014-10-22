package com.galvarez.ttw.rendering.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;

public class FramedMenu {

  private int nbColumns = 2;

  private Image frame;

  private ScrollPane scrollPane;

  private final Table table;

  private final Skin skin;

  /** max's represent the largest we want the menu getting */
  private final float maxHeight, maxWidth;

  /** The parent menu is the one we will focus on if the user closes this one */
  private final FramedMenu parent;

  public FramedMenu(Skin skin, float maxWidth, float maxHeight) {
    this(skin, maxWidth, maxHeight, null);
  }

  public FramedMenu(Skin skin, float maxWidth, float maxHeight, FramedMenu parent) {
    this.skin = skin;

    this.table = new Table();
    table.defaults().expandX().fillX().left();

    this.maxHeight = maxHeight;
    this.maxWidth = maxWidth;
    this.parent = parent;
  }

  public FramedMenu nbColumns(int nb) {
    this.nbColumns = nb;
    return this;
  }

  /** Use only for specific menus. */
  public Skin getSkin() {
    return skin;
  }

  /** Use only for specific menus. */
  public Table getTable() {
    return table;
  }

  public <E extends Enum<E>> void addSelectBox(String label, E selected, E[] values, ChangeListener lis) {
    LabelStyle style = skin.get(LabelStyle.class);
    Label l = new Label(label, style);
    table.add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

    SelectBox<E> sb = new SelectBox<E>(skin.get(SelectBoxStyle.class));
    sb.setItems(values);
    sb.setSelected(selected);
    sb.addListener(lis);
    Cell<SelectBox<E>> right = table.add(sb).minHeight(sb.getMinHeight()).prefHeight(sb.getMinHeight());
    if (nbColumns > 2)
      right.colspan(nbColumns - 1);

    table.row();
  }

  public void addTextField(String label, Object value, TextFieldListener lis) {
    Label l = new Label(label, skin.get(LabelStyle.class));
    table.add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

    TextField t = new TextField(String.valueOf(value), skin.get(TextFieldStyle.class));
    t.setTextFieldListener(lis);
    t.setDisabled(false);
    t.setMaxLength(6);
    if (value instanceof Number)
      t.setTextFieldFilter((textField, c) -> Character.isDigit(c));
    table.add(t).right().padLeft(1f);

    table.row();
  }

  /** Adds a button to the menu */
  public void addButton(String label, ChangeListener listener, boolean active) {
    addButton(label, "", listener, active);
  }

  /**
   * Adds a button to the menu, with a secondary label (like MP cost) aligned to
   * the right
   */
  public void addButton(String label, String secondaryLabel, ChangeListener listener, boolean active) {
    LabelStyle style = active ? skin.get(LabelStyle.class) : skin.get("inactive", LabelStyle.class);

    Button b = new Button(skin.get(ButtonStyle.class));
    b.addListener(listener);
    b.setDisabled(!active);

    b.add(new Label(label, style)).left();
    b.add(new Label(secondaryLabel, style)).padRight(15f);

    table.add(b).left().padLeft(1f);
    table.row();
  }

  /** Adds a label followed by a sprite to the menu. */
  public Label addLabelSprite(String label, TextureRegion region, Color color) {
    LabelStyle style = skin.get(LabelStyle.class);
    style.font.setMarkupEnabled(true);
    Label l = new Label(label, style);
    table.add(l).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());

    Image i = new Image(region);
    i.setColor(color);
    i.setScaling(Scaling.fit);
    Cell<Image> right = table.add(i).minHeight(region.getRegionHeight()).prefHeight(region.getRegionHeight()).right();
    if (nbColumns > 2)
      right.colspan(nbColumns - 1);

    table.row();
    return l;
  }

  /** Adds a label to the menu. */
  public Label addLabel(String label) {
    LabelStyle style = skin.get(LabelStyle.class);
    style.font.setMarkupEnabled(true);
    Label l = new Label(label, style);

    table.add(l).colspan(nbColumns).minHeight(l.getMinHeight()).prefHeight(l.getPrefHeight());
    table.row();

    return l;
  }

  /** Adds a sprite to the menu */
  public void addSprite(TextureRegion region) {
    Image i = new Image(region);
    i.setScaling(Scaling.fit);

    table.add(i).colspan(nbColumns).minHeight(region.getRegionHeight()).prefHeight(region.getRegionHeight());
    table.row();
  }

  /**
   * Adds the frame and scrollpane to the specified stage at the specified
   * location. Sizes the scrollpane to the (estimated) table size, up to a
   * maximum given in the constructor.
   * 
   * @param canEscape if true pressing on ESC key will close the menu
   */
  public void addToStage(final Stage stage, float x, float y, boolean canEscape) {
    scrollPane = new ScrollPane(table, skin);
    frame = new Image(skin.getPatch("frame"));

    if (canEscape) {
      // If the user presses "ESC", close this menu and focus on the "parent"
      stage.setKeyboardFocus(scrollPane);
      scrollPane.addListener(new InputListener() {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
          if (keycode == Keys.ESCAPE) {
            // If this menu is invisible, don't do anything
            if (!frame.isVisible())
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
    stage.addActor(frame);

    // having a background in the table would prevent us from using the correct
    // preferred size as the table is at least as big as its background
    // TODO avoid this shit
    table.setBackground((Drawable) null);

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

    // For now, no matter what, the width is set to maxWidth
    scrollPane.setWidth(maxWidth);

    table.setBackground(skin.getTiledDrawable("menuTexture"));

    // Move the table to the far left of the scrollPane
    table.left();

    // Prevent the scrollPane from scrolling (and snapping back) beyond the
    // scroll limits
    scrollPane.setOverscroll(false, false);
    scrollPane.setFillParent(false);
    // If y is negative, center the scrollPane vertically on the stage
    if (y < 0)
      scrollPane.setY((stage.getHeight() - scrollPane.getHeight()) / 2f);
    else
      scrollPane.setY(y - scrollPane.getHeight());
    // If x is negative, do likewise
    if (x < 0)
      scrollPane.setX((stage.getWidth() - scrollPane.getWidth()) / 2f);
    else
      scrollPane.setX(x);

    // Make sure we can't touch the frame - that would make the scrollPane
    // inaccessible
    frame.setTouchable(Touchable.disabled);

    // Now set the Frame's position and size based on the scrollPane's stuff
    frame.setX(scrollPane.getX() - 1);
    frame.setY(scrollPane.getY() - 3);
    frame.setWidth(scrollPane.getWidth() + 4);
    frame.setHeight(scrollPane.getHeight() + 4);

    // In case they became invisible earlier, make them visible now
    scrollPane.setVisible(true);
    frame.setVisible(true);
  }

  /** Wipe all the buttons off, and remove widgets from stage. */
  public void clear() {
    table.clear();
    table.setColor(1f, 1f, 1f, 1);
    if (scrollPane != null)
      scrollPane.remove();
    if (frame != null)
      frame.remove();
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
    if (frame == null)
      return;
    frame.setVisible(visible);
    scrollPane.setVisible(visible);
  }

  /** Let someone else know who your parent is - currently used in MenuBuilder */
  public FramedMenu getParent() {
    return parent;
  }

}
