package com.galvarez.ttw;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.PooledComponent;
import com.artemis.systems.EntityProcessingSystem;

public class ExpiringSystem extends EntityProcessingSystem {

  ComponentMapper<Expires> em;

  @SuppressWarnings("unchecked")
  public ExpiringSystem() {
    super(Aspect.getAspectForAll(Expires.class));
  }

  @Override
  public void initialize() {
    em = world.getMapper(Expires.class);
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity e) {
    Expires exp = em.get(e);
    exp.delay -= world.getDelta();
    if (exp.delay <= 0) {
      e.deleteFromWorld();
    }
  }

  public static final class Expires extends PooledComponent {

    public float delay;

    public Expires() {
    }

    @Override
    public void reset() {
      delay = 0f;
    }
  }

}
