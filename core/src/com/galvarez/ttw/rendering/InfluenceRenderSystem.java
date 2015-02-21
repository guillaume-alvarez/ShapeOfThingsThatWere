package com.galvarez.ttw.rendering;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.model.map.MapTools.Border;
import com.galvarez.ttw.utils.FloatPair;

@Wire
public final class InfluenceRenderSystem extends AbstractRendererSystem {

  private ComponentMapper<Empire> empires;

  private final GameMap map;

  private final EnumMap<Border, AtlasRegion> borderTexture = new EnumMap<>(Border.class);

  private final AtlasRegion blank;

  public InfluenceRenderSystem(OrthographicCamera camera, SpriteBatch batch, GameMap map) {
    super(with(InfluenceSource.class), camera, batch);
    this.map = map;

    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("textures/maptiles.atlas"), Gdx.files.internal("textures"));

    blank = atlas.findRegion("hex_blank");

    borderTexture.put(Border.BOTTOM_LEFT, atlas.findRegion("border_bottom_left"));
    borderTexture.put(Border.BOTTOM_RIGHT, atlas.findRegion("border_bottom_right"));
    borderTexture.put(Border.BOTTOM, atlas.findRegion("border_bottom"));
    borderTexture.put(Border.TOP_LEFT, atlas.findRegion("border_top_left"));
    borderTexture.put(Border.TOP_RIGHT, atlas.findRegion("border_top_right"));
    borderTexture.put(Border.TOP, atlas.findRegion("border_top"));
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  private final Map<Empire, List<InfluenceBorder>> borders = new HashMap<>();

  private static final class InfluenceBorder {

    private final Border[] borders;

    private final MapPosition position;

    public InfluenceBorder(MapPosition position, Border ... borders) {
      this.position = position;
      this.borders = borders;
    }

  }

  private final Map<MapPosition, Float> colors = new HashMap<>();

  private volatile boolean displayColoredInfluence = true;

  /**
   * Collect the borders for every influence source. Should be done only when
   * the borders changed, meaning after processing a turn.
   */
  public void preprocess() {
    borders.clear();
    colors.clear();
    List<Border> tmp = new ArrayList<>();
    for (int x = 0; x < map.map.length; x++) {
      for (int y = 0; y < map.map[0].length; y++) {
        Influence inf = map.getInfluenceAt(x, y);
        Entity source = inf.getMainInfluenceSource(world);
        if (source != null) {
          Empire empire = empires.get(source);

          // collect borders
          for (Border b : Border.values()) {
            MapPosition neighbor = b.getNeighbor(x, y);
            Influence neighborTile = map.getInfluenceAt(neighbor);
            if (neighborTile == null || empire != getMainEmpire(neighborTile))
              tmp.add(b);
          }
          if (!tmp.isEmpty()) {
            List<InfluenceBorder> list = borders.get(empire);
            if (list == null)
              borders.put(empire, list = new ArrayList<>());
            list.add(new InfluenceBorder(map.getPositionAt(x, y), tmp.toArray(new Border[tmp.size()])));
            tmp.clear();
          }

          // collect color
          Color c = empire.color;
          colors.put(inf.position, Color.toFloatBits(c.r, c.g, c.b, inf.getMaxInfluence() / 300f));
        }
      }
    }
  }

  private Empire getMainEmpire(Influence tile) {
    Entity main = tile.getMainInfluenceSource(world);
    return main == null ? null : empires.get(main);
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    Color c = batch.getColor();

    // draw all the pre-processed borders
    for (Empire empire : borders.keySet()) {
      // use source color
      batch.setColor(empire.color);

      for (InfluenceBorder ib : borders.get(empire))
        for (Border b : ib.borders)
          draw(borderTexture.get(b), ib.position);
    }

    if (displayColoredInfluence)
      for (Entry<MapPosition, Float> e : colors.entrySet()) {
        batch.setColor(e.getValue().floatValue());
        draw(blank, e.getKey());
      }

    // revert to previous (may be it is the last source?)
    batch.setColor(c);
  }

  private void draw(AtlasRegion reg, MapPosition p) {
    FloatPair position = MapTools.world2window(p);
    batch.draw(reg, position.x - reg.getRegionWidth() / 2, position.y - reg.getRegionHeight() / 2);
  }

  public void displayColoredInfluence(boolean b) {
    displayColoredInfluence = b;
  }

  public boolean displayColoredInfluence() {
    return displayColoredInfluence;
  }
}
