package com.galvarez.ttw.rendering;

import java.util.EnumMap;

import com.artemis.annotations.Wire;
import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

@Wire
public final class IconsSystem extends VoidEntitySystem {

  public enum Type {
    DISCOVERY, DIPLOMACY, BUILDINGS, REVOLT, FLAG, MILITARY, DISEASE;
  }

  private final EnumMap<Type, Drawable> drawables;

  private final EnumMap<Type, TextureRegion> regions;

  private final Skin skin;

  public IconsSystem() {
    skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

    this.drawables = new EnumMap<>(Type.class);
    this.regions = new EnumMap<>(Type.class);
    addIcon(Type.DISCOVERY, "discovery-bulb");
    addIcon(Type.DISCOVERY, "discovery-bulb");
    addIcon(Type.DIPLOMACY, "diplomacy-handshake");
    addIcon(Type.BUILDINGS, "buildings-hammer");
    addIcon(Type.REVOLT, "revolt-clenched-fist");
    addIcon(Type.FLAG, "finish-flag");
    addIcon(Type.MILITARY, "military-swords");
    addIcon(Type.DISEASE, "disease-skull");
  }

  private void addIcon(Type type, String name) {
    drawables.put(type, skin.getDrawable(name));
    regions.put(type, skin.getRegion(name));
  }

  @Override
  protected boolean checkProcessing() {
    return false;
  }

  @Override
  protected void processSystem() {
  }

  public Drawable get(Type type) {
    return drawables.get(type);
  }

  public TextureRegion getTexture(Type type) {
    return regions.get(type);
  }

}
