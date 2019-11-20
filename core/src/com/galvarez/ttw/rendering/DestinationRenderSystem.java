package com.galvarez.ttw.rendering;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.utils.FloatPair;

@Wire
public final class DestinationRenderSystem extends EntitySystem {

  private ComponentMapper<Empire> empires;

  private ComponentMapper<Destination> destinations;

  private ComponentMapper<MapPosition> positions;

  private final AtlasRegion flagTexture;

  private final ShapeRenderer renderer;

  private final OrthographicCamera camera;

  private final SpriteBatch batch;

  public DestinationRenderSystem(OrthographicCamera camera, SpriteBatch batch) {
    super(Aspect.getAspectForAll(Destination.class));
    this.camera = camera;
    this.batch = batch;
    this.renderer = new ShapeRenderer();

    flagTexture = new TextureAtlas(Gdx.files.internal("textures/characters.atlas"), Gdx.files.internal("textures"))
        .findRegion("flag");
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    for (Entity e : entities)
      processTargetsFlags(e);
    batch.end();
    batch.setColor(1f, 1f, 1f, 1f);

    renderer.setProjectionMatrix(camera.combined);
    renderer.begin(ShapeType.Line);
    for (Entity e : entities)
      processPathLines(e);
    renderer.end();
  }

  private void processTargetsFlags(Entity e) {
    Destination dest = destinations.get(e);

    // use source color
    Color c = batch.getColor().cpy();
    batch.setColor(empires.get(e).color);
    // draw the flag for influencing
    if (dest.target != null)
      draw(flagTexture, dest.target);
    // revert to previous (may be it is the last movement?)
    batch.setColor(c);
  }

  private void processPathLines(Entity e) {
    Destination dest = destinations.get(e);

    // draw the path line
    if (dest.path != null) {
      renderer.setColor(empires.get(e).color);
      MapPosition start = positions.get(e);
      for (MapPosition next : dest.path) {
        FloatPair startScreen = MapTools.world2window(start);
        FloatPair nextScreen = MapTools.world2window(next);
        renderer.line(startScreen.x, startScreen.y, nextScreen.x, nextScreen.y);
        start = next;
      }
    }
  }

  private void draw(AtlasRegion reg, MapPosition p) {
    FloatPair position = MapTools.world2window(p);
    batch.draw(reg, position.x - reg.getRegionWidth() / 2, position.y - reg.getRegionHeight() / 2);
  }
}
