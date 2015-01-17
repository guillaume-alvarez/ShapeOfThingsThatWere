package com.galvarez.ttw.rendering;

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
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.utils.FloatPair;

@Wire
public final class DestinationRenderSystem extends AbstractRendererSystem {

  private ComponentMapper<InfluenceSource> sources;

  private ComponentMapper<Empire> empires;

  private ComponentMapper<Destination> destinations;

  private final GameMap map;

  private final AtlasRegion flagTexture;

  public DestinationRenderSystem(OrthographicCamera camera, SpriteBatch batch, GameMap map) {
    super(with(Destination.class), camera, batch);
    this.map = map;

    flagTexture = new TextureAtlas(Gdx.files.internal("textures/characters.atlas"), Gdx.files.internal("textures"))
        .findRegion("flag");
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  private Empire empire(Entity source) {
    return empires.get(sources.get(source).empire);
  }

  private Empire getMainEmpire(Influence tile) {
    Entity main = tile.getMainInfluenceSource(world);
    return main == null ? null : empire(main);
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity e : entities)
      process(e);
  }

  private void process(Entity e) {
    Destination dest = destinations.get(e);

    // use source color
    Color c = batch.getColor();
    batch.setColor(empireColor(e));

    // draw the flag for influencing
    if (dest.target != null) {
      MapPosition pos = dest.target;
      draw(flagTexture, pos.x, pos.y);
    }

    // revert to previous (may be it is the last movement?)
    batch.setColor(c);
  }

  private Color empireColor(Entity e) {
    return empires.get(sources.get(e).empire).color;
  }

  private void draw(AtlasRegion reg, int x, int y) {
    FloatPair position = MapTools.world2window(x, y);
    batch.draw(reg, position.x - reg.getRegionWidth() / 2, position.y - reg.getRegionHeight() / 2);
  }
}
