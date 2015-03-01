package com.galvarez.ttw.rendering;

import java.util.ArrayList;
import java.util.List;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.components.Sprite;
import com.galvarez.ttw.rendering.components.SpriteAnimation;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.Font;

@Wire
public final class SpriteRenderSystem extends AbstractRendererSystem {

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Sprite> sprites;

  private ComponentMapper<SpriteAnimation> animations;

  private ComponentMapper<Name> names;

  private final TextureAtlas atlas;

  private final List<Entity> sortedEntities = new ArrayList<Entity>();

  private final BitmapFont font;

  public SpriteRenderSystem(OrthographicCamera camera, SpriteBatch batch) {
    super(with(MapPosition.class, Sprite.class), camera, batch);

    atlas = new TextureAtlas(Gdx.files.internal("textures/characters.atlas"), Gdx.files.internal("textures"));

    font = Font.IRIS_UPC.get();
    font.setUseIntegerPositions(false);
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity e : sortedEntities)
      process(e);
  }

  private void process(Entity e) {
    if (positions.has(e)) {
      MapPosition position = positions.getSafe(e);

      // If sprite is off-screen, don't bother drawing it!
      if (!isOnScreen(position))
        return;

      Sprite sprite = sprites.get(e);

      TextureRegion spriteRegion = sprite.region;
      batch.setColor(sprite.color);

      int width = spriteRegion.getRegionWidth();
      int height = spriteRegion.getRegionHeight();

      sprite.region.setRegion(sprite.x, sprite.y, width, height);

      FloatPair drawPosition = MapTools.world2window(position);
      float posX = drawPosition.x - (spriteRegion.getRegionWidth() / 2 * sprite.scaleX);
      float posY = drawPosition.y - (spriteRegion.getRegionHeight() / 2 * sprite.scaleY);

      batch.draw(spriteRegion, posX, posY, 0, 0, spriteRegion.getRegionWidth(), spriteRegion.getRegionHeight(),
          sprite.scaleX, sprite.scaleY, sprite.rotation);
    }
  }

  @Override
  protected void inserted(Entity e) {
    sortedEntities.add(e);
    Sprite sprite = sprites.get(e);
    TextureRegion reg = atlas.findRegion(sprite.name);
    sprite.region = reg;
    sprite.x = reg.getRegionX();
    sprite.y = reg.getRegionY();
    sprite.width = reg.getRegionWidth();
    sprite.height = reg.getRegionHeight();
    if (animations.has(e)) {
      SpriteAnimation anim = animations.getSafe(e);
      anim.animation = new Animation(anim.frameDuration, atlas.findRegions(sprite.name), anim.playMode);
    }

    sortedEntities.sort((e1, e2) -> sprites.get(e1).layer.compareTo(sprites.get(e2).layer));
  }

  @Override
  protected void removed(Entity e) {
    sortedEntities.remove(e);
  }
}
