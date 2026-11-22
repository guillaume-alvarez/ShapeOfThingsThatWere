package com.galvarez.ttw.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


public final class Assets implements Disposable {

    public static final String FONTS_ROMANICA_TTF = "fonts/Romanica.ttf";
    public static final String UISKIN_ATLAS = "uiskin/uiskin.atlas";
    public static final String UISKIN_JSON = "uiskin/uiskin.json";

    private final TextureAtlas uiAtlas;

    public enum Icon {
        DISCOVERY("discovery-bulb"),
        DIPLOMACY("diplomacy-handshake"),
        BUILDINGS("buildings-hammer"),
        REVOLT("revolt-clenched-fist"),
        FLAG("finish-flag"),
        MILITARY("military-swords"),
        DISEASE("disease-skull"),
        END_TURN("empty-hourglass");

        private final String name;

        Icon(String name) {
            this.name = name;
        }
    }

    private final AssetManager manager;

    private final EnumMap<Icon, Drawable> drawables = new EnumMap<>(Icon.class);

    private final EnumMap<Icon, TextureRegion> regions = new EnumMap<>(Icon.class);

    private final Map<String, BitmapFont> fonts = new HashMap<>();

    public Assets() {
        manager = loadAssets();
        uiAtlas = manager.get(UISKIN_ATLAS, TextureAtlas.class);

        ObjectMap<String, Object> fonts = new ObjectMap<>();
        fonts.put("default-font", getFont(16));
        fonts.put("colored-font", getFont(16, true));
        manager.load(UISKIN_JSON, Skin.class, new SkinLoader.SkinParameter(fonts));
        manager.finishLoading();

        if (VisUI.isLoaded()) {
            VisUI.dispose();
        }
        VisUI.load();
        VisUI.getSkin().addRegions(uiAtlas);
        VisSkinConfiguration.apply(manager.get(UISKIN_JSON));

        for (Icon icon : Icon.values())
            addIcon(icon, icon.name);
    }

    private static AssetManager loadAssets() {
        AssetManager manager = new AssetManager();
        manager.load(UISKIN_ATLAS, TextureAtlas.class);
        manager.finishLoading();
        return manager;
    }

    private void addIcon(Icon type, String name) {
        if (VisUI.getSkin().has(name, TextureRegion.class)) {
            drawables.put(type, VisUI.getSkin().getDrawable(name));
            regions.put(type, VisUI.getSkin().getRegion(name));
        }
    }

    public BitmapFont getFont(int size) {
        return getFont(size, false);
    }

    public BitmapFont getFont(int size, boolean markupEnabled) {
        String key = "romanica-" + size + "-" + markupEnabled;
        synchronized (fonts) {
            BitmapFont font = fonts.get(key);
            if (font == null) {
                fonts.put(key, font = createFont(size, markupEnabled));
            }
            return font;
        }
    }

    private static BitmapFont createFont(int size, boolean markupEnabled) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTS_ROMANICA_TTF));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        try {
            parameter.size = size;
            BitmapFont font = generator.generateFont(parameter);
            font.getData().markupEnabled = markupEnabled;
            return font;
        } finally {
            generator.dispose(); // don't forget to dispose to avoid memory leaks!
        }
    }

    public Drawable getDrawable(Icon type) {
        return drawables.get(type);
    }

    public TextureRegion getTexture(Icon type) {
        return regions.get(type);
    }

    public Skin getSkin() {
        return VisUI.getSkin();
    }


    @Override
    public void dispose() {
        VisUI.dispose();
        manager.dispose();
    }
}

