package com.galvarez.ttw.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Utility methods to use fonts.
 * 
 * @author Guillaume Alvarez
 */
public enum Font {

  NORMAL("normal"), IRIS_UPC("irisUPC");

  private final String file;

  Font(String file) {
    this.file = file;
  }

  public BitmapFont get() {
    Texture fontTexture = new Texture(Gdx.files.internal("fonts/" + file + ".png"));
    fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
    TextureRegion fontRegion = new TextureRegion(fontTexture);
    return new BitmapFont(Gdx.files.internal("fonts/" + file + ".fnt"), fontRegion, false);
  }

  public Pixmap getPM() {
    return new Pixmap(Gdx.files.internal("fonts/" + file + ".png"));
  }

  public BitmapFontData getData(boolean flip) {
    return new BitmapFont.BitmapFontData(Gdx.files.internal("fonts/" + file + ".fnt"), flip);
  }

}
