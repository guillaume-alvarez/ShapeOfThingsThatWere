package com.galvarez.ttw.rendering;

import java.util.ArrayList;
import java.util.Comparator;
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
import com.galvarez.ttw.rendering.components.Sprite;
import com.galvarez.ttw.rendering.components.SpriteAnimation;
import com.galvarez.ttw.utils.Assets;
import com.galvarez.ttw.utils.FloatPair;
import com.galvarez.ttw.utils.Font;

import static java.util.Comparator.comparing;

@Wire
public final class SpriteRenderSystem extends AbstractRendererSystem {

  private ComponentMapper<MapPosition> positions;

  private ComponentMapper<Sprite> sprites;

  private ComponentMapper<SpriteAnimation> animations;

  private final TextureAtlas atlas;

  private final List<Entity> sortedEntities = new ArrayList<>();

  public SpriteRenderSystem(OrthographicCamera camera, SpriteBatch batch) {
    super(with(MapPosition.class, Sprite.class), camera, batch);

    atlas = new TextureAtlas(Gdx.files.internal("textures/characters.atlas"), Gdx.files.internal("textures"));
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
      float posX = drawPosition.x - ((spriteRegion.getRegionWidth() / 2) * sprite.scaleX);
      float posY = drawPosition.y - ((spriteRegion.getRegionHeight() / 2) * sprite.scaleY);

      batch.draw(spriteRegion, posX, posY, 0, 0, spriteRegion.getRegionWidth(), spriteRegion.getRegionHeight(),
          sprite.scaleX, sprite.scaleY, sprite.rotation);
    }
  }

  @Override
  protected void inserted(Entity entity) {
    sortedEntities.add(entity);
    Sprite sprite = sprites.get(entity);
    initSprite(sprite);

    sortedEntities.sort(comparing(e -> sprites.get(e).layer));
  }

  public void setSprite(Entity e, String spriteName) {
    Sprite sprite = sprites.get(e);
    sprite.name = spriteName;
    initSprite(sprite);
  }

  private void initSprite(Sprite sprite) {
    TextureRegion reg = atlas.findRegion(sprite.name);
    sprite.region = reg;
    sprite.x = reg.getRegionX();
    sprite.y = reg.getRegionY();
    sprite.width = reg.getRegionWidth();
    sprite.height = reg.getRegionHeight();
  }

  @Override
  protected void removed(Entity e) {
    sortedEntities.remove(e);
  }
}
