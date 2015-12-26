package com.galvarez.ttw.rendering;

import java.util.ArrayList;
import java.util.List;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.rendering.components.TextBox;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.Font;

@Wire
public final class TextBoxRenderSystem extends AbstractRendererSystem {

  private static final int PAD = 2;

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<TextBox> boxes;

  private ComponentMapper<Empire> empires;

  private final List<Entity> entities = new ArrayList<>();

  private final BitmapFont font;

  public TextBoxRenderSystem(OrthographicCamera camera, SpriteBatch batch) {
    super(with(MapPosition.class, TextBox.class), camera, batch);

    font = Font.IRIS_UPC.get();
    font.setUseIntegerPositions(false);
  }

  @Override
  protected void inserted(Entity e) {
    entities.add(e);
  }

  @Override
  protected void removed(Entity e) {
    entities.remove(e);
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  /** Update the text for all the entities. */
  public void preprocess() {
    for (Entity e : entities) {
      TextBox box = boxes.get(e);
      box.text = box.generator.get();

      Empire empire = empires.get(e);

      GlyphLayout bounds = font.getCache().setText(box.text, 0, 0);
      Pixmap pm = new Pixmap((int) bounds.width + PAD * 2, (int) bounds.height + PAD * 2, Pixmap.Format.RGBA8888);
      pm.setColor(empire.backColor);
      pm.fill();
      pm.setColor(empire.color);
      pm.drawRectangle(0, 0, pm.getWidth(), pm.getHeight());
      box.texture = new Texture(pm);
      box.color = empire.color;
    }
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> list) {
    for (Entity e : entities)
      process(e);
  }

  private void process(Entity e) {
    if (positions.has(e)) {
      MapPosition position = positions.getSafe(e);

      // If sprite is off-screen, don't bother drawing it!
      if (!isOnScreen(position))
        return;

      TextBox box = boxes.get(e);
      Texture texture = box.texture;

      int width = texture.getWidth();
      int height = texture.getHeight();

      FloatPair drawPosition = MapTools.world2window(position);
      float posX = drawPosition.x - (width / 2);
      float posY = drawPosition.y + height;

      batch.draw(texture, posX, posY);

      posX += PAD;
      posY += texture.getHeight() - PAD / 2;

      font.setColor(box.color);
      font.draw(batch, box.text, posX, posY);
    }
  }
}
