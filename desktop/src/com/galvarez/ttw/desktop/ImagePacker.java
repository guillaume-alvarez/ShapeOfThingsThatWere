package com.galvarez.ttw.desktop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class ImagePacker {

  public static void run() {
    Settings settings = new Settings();
    settings.filterMin = Texture.TextureFilter.Linear;
    settings.filterMag = Texture.TextureFilter.Linear;
    settings.pot = false;

    TexturePacker.process(settings, "textures/characters", "resources/textures", "characters");
    TexturePacker.process(settings, "textures/maptiles", "resources/textures", "maptiles");
    TexturePacker.process(settings, "textures/uiskin", "resources/uiskin", "uiskin");
  }

  public static void main(String[] args) {
    run();
  }
}
