package com.galvarez.ttw.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip.TextTooltipStyle;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox.VisCheckBoxStyle;
import com.kotcrab.vis.ui.widget.VisTextButton.VisTextButtonStyle;
import com.kotcrab.vis.ui.widget.VisTextField.VisTextFieldStyle;

/**
 * Applies the game's Romanica typography and parchment/marble visuals to the
 * VisUI skin. VisUI widgets obtain their styles from {@link VisUI#getSkin()},
 * so this class must run after {@link VisUI#load()} and before any widget is
 * constructed.
 */
public final class VisSkinConfiguration {

    public static final String DEFAULT_FONT = "default-font";
    public static final String COLORED_FONT = "colored-font";
    public static final String SMALL_FONT = "small-font";
    public static final String DEFAULT_LABEL = "default";
    public static final String COLORED_LABEL = "colored";
    public static final String INACTIVE_LABEL = "inactive";
    public static final String DEFAULT_BUTTON = "default";
    public static final String INACTIVE_BUTTON = "inactive";
    public static final String FRAME_BACKGROUND = "frame";
    public static final String MARBLE_BACKGROUND = "big-marble-texture";
    public static final String PARCHMENT_BACKGROUND = "parch-texture";

    private static final Color BLACK = new Color(0f, 0f, 0f, 1f);
    private static final Color GRAY = new Color(0.35f, 0.35f, 0.35f, 1f);
    private static final Color WHITE = new Color(1f, 1f, 1f, 1f);
    private static final Color RED = new Color(1f, 0f, 0f, 1f);

    private VisSkinConfiguration() {
    }

    /**
     * Merges the game skin into the already loaded VisUI skin. Existing VisUI
     * styles are retained so its widgets remain functional.
     */
    public static void apply(Skin gameSkin) {
        Skin skin = VisUI.getSkin();

        addGameFonts(skin, gameSkin);
        addGameColors(skin);
        addGameDrawables(skin, gameSkin);
        addStyles(skin, gameSkin);
    }

    private static void addGameFonts(Skin skin, Skin gameSkin) {
        BitmapFont font = font(gameSkin, DEFAULT_FONT);
        BitmapFont coloredFont = font(gameSkin, COLORED_FONT);
        BitmapFont smallFont = font(gameSkin, SMALL_FONT);

        if (font == null || coloredFont == null) {
            throw new IllegalStateException("Game skin is missing Romanica fonts");
        }
        if (smallFont == null) {
            smallFont = font;
        }

        skin.add(DEFAULT_FONT, font);
        skin.add(COLORED_FONT, coloredFont);
        skin.add(SMALL_FONT, smallFont);
    }

    private static BitmapFont font(Skin skin, String name) {
        return skin.has(name, BitmapFont.class) ? skin.getFont(name) : null;
    }

    private static void addGameColors(Skin skin) {
        skin.add("black", BLACK);
        skin.add("gray", GRAY);
        skin.add("white", WHITE);
        skin.add("red", RED);
    }

    private static void addGameDrawables(Skin skin, Skin gameSkin) {
        copyDrawable(skin, gameSkin, "frame");
        copyDrawable(skin, gameSkin, "big-marble-texture");
        copyDrawable(skin, gameSkin, "parch-texture");
        copyDrawable(skin, gameSkin, "scrollBar");
        copyDrawable(skin, gameSkin, "scrollKnob");
        copyDrawable(skin, gameSkin, "check-on");
        copyDrawable(skin, gameSkin, "check-off");
    }

    private static void copyDrawable(Skin target, Skin source, String name) {
        Drawable drawable = drawable(target, name);
        if (drawable == null) {
            drawable = drawable(source, name);
        }
        if (drawable != null) {
            target.add(name, drawable);
        }
    }

    private static Drawable drawable(Skin skin, String name) {
        try {
            return skin.getDrawable(name);
        } catch (GdxRuntimeException e) {
            return null;
        }
    }

    /**
     * Builds a hover/pressed background that keeps the button frame while
     * overlaying a translucent grey, so the marble behind the menu remains
     * visible.
     */
    private static Drawable overlay(Skin skin, Drawable base, float alpha) {
        Drawable white = drawable(skin, "white");
        if (white == null) {
            return base;
        }
        return new ShadedDrawable(white, new Color(0.35f, 0.35f, 0.35f, alpha), base);
    }

    private static final class ShadedDrawable extends BaseDrawable {

        private final Drawable shade;
        private final Color shadeColor;
        private final Drawable base;

        private ShadedDrawable(Drawable shade, Color shadeColor, Drawable base) {
            this.shade = shade;
            this.shadeColor = shadeColor;
            this.base = base;
        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
            Color previous = batch.getColor();
            batch.setColor(shadeColor);
            shade.draw(batch, x, y, width, height);
            batch.setColor(previous);
            base.draw(batch, x, y, width, height);
        }
    }

    private static void addStyles(Skin skin, Skin gameSkin) {
        BitmapFont font = gameSkin.getFont(DEFAULT_FONT);
        BitmapFont smallFont = font(gameSkin, SMALL_FONT);
        if (smallFont == null) {
            smallFont = font;
        }

        skin.add(DEFAULT_LABEL, new LabelStyle(font, BLACK));
        skin.add(COLORED_LABEL, new LabelStyle(font(gameSkin, COLORED_FONT), WHITE));
        skin.add(INACTIVE_LABEL, new LabelStyle(font, GRAY));
        skin.add("small", new LabelStyle(smallFont, WHITE));
        if (skin.has("link-label", Color.class)) {
            skin.add("link-label", new LabelStyle(font, skin.getColor("link-label")));
        }
        if (skin.has("menuitem", Color.class)) {
            skin.add("menuitem-shortcut", new LabelStyle(font, skin.getColor("menuitem")));
        }

        Drawable frame = drawable(skin, "frame");
        Drawable hover = frame == null ? null : overlay(skin, frame, 0.30f);
        Drawable pressed = frame == null ? null : overlay(skin, frame, 0.45f);

        VisTextButtonStyle button = new VisTextButtonStyle();
        button.font = font;
        button.fontColor = BLACK;
        button.overFontColor = BLACK;
        button.downFontColor = BLACK;
        button.checkedFontColor = BLACK;
        button.disabledFontColor = GRAY;
        button.up = frame;
        button.down = pressed;
        button.checked = hover;
        button.over = hover;
        button.disabled = frame;
        button.focusBorder = frame;
        skin.add(DEFAULT_BUTTON, button);

        VisTextButtonStyle inactiveButton = new VisTextButtonStyle(button);
        inactiveButton.fontColor = GRAY;
        inactiveButton.overFontColor = GRAY;
        inactiveButton.downFontColor = GRAY;
        inactiveButton.checkedFontColor = GRAY;
        skin.add(INACTIVE_BUTTON, inactiveButton);

        VisCheckBoxStyle checkBox = new VisCheckBoxStyle();
        checkBox.font = font;
        checkBox.fontColor = BLACK;
        checkBox.disabledFontColor = GRAY;
        checkBox.checkBackground = drawable(skin, "check-off");
        checkBox.checkBackgroundOver = drawable(skin, "check-off");
        checkBox.checkBackgroundDown = drawable(skin, "check-off");
        checkBox.tick = drawable(skin, "check-on");
        checkBox.tickDisabled = drawable(skin, "check-on");
        checkBox.focusBorder = drawable(skin, "border");
        checkBox.errorBorder = drawable(skin, "border-error");
        skin.add("default", checkBox);

        VisTextFieldStyle textField = new VisTextFieldStyle();
        textField.font = font;
        textField.fontColor = BLACK;
        textField.disabledFontColor = GRAY;
        textField.focusedFontColor = RED;
        textField.background = drawable(skin, "textfield");
        textField.backgroundOver = drawable(skin, "textfield-over");
        textField.cursor = drawable(skin, "cursor");
        textField.selection = drawable(skin, "selection");
        textField.focusBorder = drawable(skin, "border");
        textField.errorBorder = drawable(skin, "border-error");
        skin.add("default", textField);

        SelectBoxStyle selectBox = new SelectBoxStyle(gameSkin.get(SelectBoxStyle.class));
        if (selectBox.listStyle != null) {
            ListStyle listStyle = new ListStyle(selectBox.listStyle);
            // the full-size marble background inflates the list height and
            // makes SelectBox scroll the first item half out of the dropdown
            listStyle.background = null;
            selectBox.listStyle = listStyle;
        }
        skin.add("default", selectBox);

        TextTooltipStyle tooltip = new TextTooltipStyle();
        tooltip.label = new LabelStyle(font, BLACK);
        tooltip.background = new NinePatchDrawable(gameSkin.getPatch(PARCHMENT_BACKGROUND));
        skin.add("default", tooltip);
    }
}