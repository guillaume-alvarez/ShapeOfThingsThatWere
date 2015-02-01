package com.galvarez.ttw.rendering;

import java.util.Arrays;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;

public abstract class AbstractRendererSystem extends EntitySystem {

  protected final OrthographicCamera camera;

  protected final SpriteBatch batch;

  public AbstractRendererSystem(Aspect aspect, OrthographicCamera camera, SpriteBatch batch) {
    super(aspect);
    this.camera = camera;
    this.batch = batch;
  }

  @SafeVarargs
  protected static Aspect with(Class<? extends Component> ... components) {
    return Aspect.getEmpty().all(Arrays.asList(components));
  }

  @Override
  protected void begin() {
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
  }

  @Override
  protected void end() {
    batch.end();
    batch.setColor(1f, 1f, 1f, 1f);
  }

  protected final boolean isOnScreen(MapPosition position) {
    // Get bottom left and top right coordinates of camera viewport and
    // convert into grid coordinates for the map
    int x0 = MathUtils.floor(camera.frustum.planePoints[0].x / MapTools.col_multiple) - 1;
    int y0 = MathUtils.floor(camera.frustum.planePoints[0].y / MapTools.row_multiple) - 1;
    int x1 = MathUtils.floor(camera.frustum.planePoints[2].x / MapTools.col_multiple) + 2;
    int y1 = MathUtils.floor(camera.frustum.planePoints[2].y / MapTools.row_multiple) + 1;
    // If sprite is off-screen, don't bother drawing it!
    return position.x >= x0 && position.x <= x1 && position.y >= y0 && position.y <= y1;
  }
}
