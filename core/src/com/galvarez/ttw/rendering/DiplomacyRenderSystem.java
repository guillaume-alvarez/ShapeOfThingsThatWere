package com.galvarez.ttw.rendering;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.galvarez.ttw.model.DiplomaticSystem.State;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.utils.FloatPair;

/**
 * Displays arrows from vassals to overlords.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class DiplomacyRenderSystem extends AbstractRendererSystem {

  private static final Logger log = LoggerFactory.getLogger(InfluenceSystem.class);

  /** Arrow height in pixels at standard zoom. */
  private static final int ARROW_HEIGHT = 4;

  /** Do not mask terrain with arrow. */
  private static final float ALPHA = 0.5f;

  private ComponentMapper<Empire> empires;

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<MapPosition> positions;

  private final float textureRatio;

  private final Texture arrow;

  public DiplomacyRenderSystem(OrthographicCamera camera, SpriteBatch batch) {
    super(with(Diplomacy.class), camera, batch);

    arrow = new Texture("textures/arrow.png");
    arrow.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);

    textureRatio = arrow.getHeight() / ARROW_HEIGHT;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  static final class Link {

    final MapPosition start;

    final MapPosition end;

    final Color color;

    Link(MapPosition start, MapPosition end, Color color) {
      this.start = start;
      this.end = end;
      this.color = color;
    }

  }

  private Link createLink(Entity start, Entity end) {
    return new Link(positions.get(start), positions.get(end), empires.get(end).color);
  }

  private final List<Link> links = new ArrayList<>();

  /**
   * Collect the borders for every influence source. Should be done only when
   * the borders changed, meaning after processing a turn.
   */
  public void preprocess() {
    links.clear();

    ImmutableBag<Entity> players = getActives();

    for (int i = 0; i < players.size(); i++) {
      Entity e1 = players.get(i);
      Diplomacy r1 = relations.get(e1);
      for (int j = i + 1; j < players.size(); j++) {
        Entity e2 = players.get(j);
        Diplomacy r2 = relations.get(e2);

        if (r1.getRelationWith(e2) == State.TRIBUTE)
          links.add(createLink(e1, e2));
        else if (r2.getRelationWith(e1) == State.TRIBUTE)
          links.add(createLink(e2, e1));
      }
    }
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    Color c = batch.getColor();

    // draw all the pre-processed links
    for (Link l : links) {
      // use source color
      batch.setColor(l.color);

      FloatPair start = MapTools.world2window(l.start);
      FloatPair end = MapTools.world2window(l.end);

      batch.draw(arrow, vertices(start, end, l.color), 0, 20);
    }

    // revert to previous (may be it is the last source?)
    batch.setColor(c);
  }

  private static final Vector2 tmp = new Vector2();

  /**
   * Grossly inspired by
   * {@link ShapeRenderer#rectLine(float, float, float, float, float)} and
   * {@link SpriteBatch#draw(Texture, float, float, float, float)}.
   */
  private float[] vertices(FloatPair start, FloatPair end, Color color) {
    // http://stackoverflow.com/questions/11954964/libgdx-spritebatch-draw-specifying-4-vertices
    // https://gist.github.com/mattdesl/4255476
    float c = Color.toFloatBits(color.r, color.g, color.b, ALPHA);

    float x1 = start.x;
    float y1 = start.y;
    float x2 = end.x;
    float y2 = end.y;

    tmp.set(y2 - y1, x1 - x2);
    float length = tmp.len();
    Vector2 t = tmp.nor();
    float tx = t.x * ARROW_HEIGHT;
    float ty = t.y * ARROW_HEIGHT;

    final float u1 = 0;
    final float v1 = 1;
    // number of times it is repeated, have it a bit longer than high
    final float u2 = (length / arrow.getWidth()) * textureRatio / 2;
    final float v2 = 0;

    float[] vertices = new float[20];
    vertices[X1] = x1 + tx;
    vertices[Y1] = y1 + ty;
    vertices[C1] = c;
    vertices[U1] = u1;
    vertices[V1] = v1;

    vertices[X2] = x1 - tx;
    vertices[Y2] = y1 - ty;
    vertices[C2] = c;
    vertices[U2] = u1;
    vertices[V2] = v2;

    vertices[X4] = x2 + tx;
    vertices[Y4] = y2 + ty;
    vertices[C4] = c;
    vertices[U4] = u2;
    vertices[V4] = v1;

    vertices[X3] = x2 - tx;
    vertices[Y3] = y2 - ty;
    vertices[C3] = c;
    vertices[U3] = u2;
    vertices[V3] = v2;

    return vertices;
  }

}
