package com.galvarez.ttw.rendering;

import java.util.Arrays;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
}
