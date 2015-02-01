package com.galvarez.ttw.rendering.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.utils.FloatPair;

public class MapRenderer extends AbstractRenderer {

  private final TextureAtlas atlas;

  private final Array<AtlasRegion> tilesTextures;

  private final GameMap map;

  public static final String TEXTURE_NAME = "hex";

  public MapRenderer(OrthographicCamera camera, SpriteBatch batch, GameMap map) {
    super(camera, batch);
    this.map = map;

    atlas = new TextureAtlas(Gdx.files.internal("textures/maptiles.atlas"), Gdx.files.internal("textures"));
    tilesTextures = atlas.findRegions(TEXTURE_NAME);
  }

  public AtlasRegion getTexture(Terrain t) {
    return tilesTextures.get(t.getTexture());
  }

  public void render() {
    begin();

    TextureRegion reg;

    // Get bottom left and top right coordinates of camera viewport and convert
    // into grid coordinates for the map
    int x0 = MathUtils.floor(camera.frustum.planePoints[0].x / MapTools.col_multiple) - 1;
    int y0 = MathUtils.floor(camera.frustum.planePoints[0].y / MapTools.row_multiple) - 1;
    int x1 = MathUtils.floor(camera.frustum.planePoints[2].x / MapTools.col_multiple) + 1;
    int y1 = MathUtils.floor(camera.frustum.planePoints[2].y / MapTools.row_multiple) + 1;

    // Restrict the grid coordinates to realistic values
    if (x0 % 2 == 1)
      x0 -= 1;
    if (x0 < 0)
      x0 = 0;
    if (x1 > map.map.length)
      x1 = map.map.length;
    if (y0 < 0)
      y0 = 0;
    if (y1 > map.map[0].length)
      y1 = map.map[0].length;

    // Loop over everything in the window to draw
    for (int row = y0; row < y1; row++) {
      for (int col = x0; col < x1; col++) {
        reg = tilesTextures.get(map.map[col][row].getTexture());
        FloatPair position = MapTools.world2window(col, row);
        batch.draw(reg, position.x - reg.getRegionWidth() / 2, position.y - reg.getRegionHeight() / 2);
      }
    }

    // Now draw the borders

    // This line can draw a small image of the whole map
    // batch.draw(gameMap.texture,0,0);

    end();
  }

}
