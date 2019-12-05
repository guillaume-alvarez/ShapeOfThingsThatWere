package com.galvarez.ttw.rendering;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.rendering.components.Counter;
import com.galvarez.ttw.utils.Assets;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.Font;

@Wire
public final class CounterRenderSystem extends AbstractRendererSystem {

  private static final int SIZE = 32;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Counter> counters;

  private final BitmapFont font;

  private final Map<Color, Texture> textures = new HashMap<>();

  @SuppressWarnings("unchecked")
  public CounterRenderSystem(Assets assets, OrthographicCamera camera, SpriteBatch batch) {
    super(Aspect.getAspectForAll(MapPosition.class, Counter.class), camera, batch);

    font = assets.getFont(14);
    font.setUseIntegerPositions(false);
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);

    Counter counter = counters.get(e);
    Texture texture = textures.get(counter.backColor);
    if (texture == null)
      textures.put(counter.backColor, texture = createTexture(counter.backColor));
    counter.background = texture;
  }

  private static Texture createTexture(Color color) {
    Pixmap pm = new Pixmap(SIZE, SIZE, Format.RGB888);
    pm.setColor(color);
    pm.fillRectangle(0, 0, SIZE, SIZE);
    Texture texture = new Texture(pm);
    pm.dispose();
    return texture;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity e : entities)
      process(e);
  }

  private void process(Entity e) {
    MapPosition position = positions.get(e);

    // If sprite is off-screen, don't bother drawing it!
    if (!isOnScreen(position))
      return;

    Counter counter = counters.get(e);

    FloatPair drawPosition = MapTools.world2window(position);
    float posX = drawPosition.x - (SIZE / 2);
    float posY = drawPosition.y - (SIZE / 2);

    batch.setColor(Color.WHITE);
    batch.draw(counter.background, posX, posY);

    posX += 2;
    posY += SIZE - 2;

    font.setColor(counter.textColor);
    font.draw(batch, Integer.toString(counter.value), posX, posY);
  }
}
