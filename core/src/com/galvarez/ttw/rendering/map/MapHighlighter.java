package com.galvarez.ttw.rendering.map;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.Font;

public class MapHighlighter extends AbstractRenderer {

  private final Texture highlight;

  private float t;

  private float r, g, b, a;

  private final BitmapFont font;

  public MapHighlighter(OrthographicCamera camera, SpriteBatch batch) {
    super(camera, batch);

    highlight = new Texture(Gdx.files.internal("textures/misc/hex_blank.png"));
    font = Font.IRIS_UPC.get();
    font.setColor(Color.BLACK);

    t = 0;
  }

  public void render(Map<MapPosition, String> tiles) {
    if (tiles == null || tiles.size() < 1)
      return;

    // Get bottom left and top right coordinates of camera viewport and convert
    // into grid coordinates for the map
    int x0 = MathUtils.floor(camera.frustum.planePoints[0].x / MapTools.col_multiple) - 1;
    int y0 = MathUtils.floor(camera.frustum.planePoints[0].y / MapTools.row_multiple);
    int x1 = MathUtils.floor(camera.frustum.planePoints[2].x / MapTools.col_multiple);
    int y1 = MathUtils.floor(camera.frustum.planePoints[2].y / MapTools.row_multiple);

    begin();
    batch.setColor(r, g, b, a / 8 * (7 + MathUtils.cos(8 * t)));

    for (Entry<MapPosition, String> e : tiles.entrySet()) {
      MapPosition tile = e.getKey();
      if (tile.x < x0 || tile.x > x1 || tile.y < y0 || tile.y > y1)
        continue;
      FloatPair coords = MapTools.world2window(tile.x, tile.y);
      batch.draw(highlight, coords.x - highlight.getWidth() / 2, coords.y - highlight.getHeight() / 2);

      // then draw the remaining cost for each cell
      String msg = e.getValue();
      font.draw(batch, msg, coords.x, coords.y);
    }

    t += Gdx.graphics.getDeltaTime();

    end();
  }

  public void setColor(float r, float g, float b, float a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }
}
