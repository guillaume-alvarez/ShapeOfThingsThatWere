package com.galvarez.ttw.rendering;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.rendering.components.FadingMessage;
import com.galvarez.ttw.rendering.components.MutableMapPosition;
import com.galvarez.ttw.utils.Assets;
import com.galvarez.ttw.utils.Assets.Icon;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.Font;

@Wire
public final class FadingMessageRenderSystem extends EntityProcessingSystem {

  private ComponentMapper<MutableMapPosition> mmpm;

  private ComponentMapper<FadingMessage> fmm;

  private final Assets assets;

  private final SpriteBatch batch;

  private final OrthographicCamera camera;

  private final BitmapFont font;

  private final GlyphLayout layout  = new GlyphLayout();

  @SuppressWarnings("unchecked")
  public FadingMessageRenderSystem(Assets assets, OrthographicCamera camera, SpriteBatch batch) {
    super(Aspect.getAspectForAll(MutableMapPosition.class, FadingMessage.class));
    this.assets = assets;
    this.batch = batch;
    this.camera = camera;

    this.font = assets.getFont(14);
  }

  public void createFadingIcon(Icon icon, Color color, MapPosition pos, float duration) {
    EntityFactory.createFadingTileIcon(world, assets.getTexture(icon), color, pos.x, pos.y, duration);
  }

  @Override
  protected void begin() {
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    batch.setColor(1, 1, 1, 1);
  }

  @Override
  protected void process(Entity e) {
    MutableMapPosition position = mmpm.get(e);
    FadingMessage message = fmm.get(e);
    FloatPair drawPosition = MapTools.world2window(position.x, position.y);

    if (message.label != null)
      drawLabel(drawPosition, message);

    if (message.icon != null)
      drawIcon(drawPosition, message);

    position.x += message.vx * world.getDelta();
    position.y += message.vy * world.getDelta();
    message.currentTime += world.getDelta();

    if (message.currentTime >= message.duration)
      e.deleteFromWorld();
  }

  private void drawLabel(FloatPair drawPosition, FadingMessage message) {
    Color color = message.color;
    font.setColor(color.r, color.g, color.b, 1f - message.currentTime / message.duration);
    layout.setText(font, message.label);

    float posX = drawPosition.x - layout.width / 2;
    float posY = drawPosition.y;
    font.draw(batch, layout, posX, posY);
  }

  private void drawIcon(FloatPair drawPosition, FadingMessage message) {
    float posX = drawPosition.x - message.icon.getRegionWidth() / 2;
    float posY = drawPosition.y;

    Color color = message.color;
    Color oldColor = batch.getColor().cpy();
    batch.setColor(color.r, color.g, color.b, 1f - message.currentTime / message.duration);
    batch.draw(message.icon, posX, posY);
    batch.setColor(oldColor);
  }

  @Override
  protected void end() {
    batch.end();
  }

}
