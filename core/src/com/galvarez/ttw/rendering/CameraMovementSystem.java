package com.galvarez.ttw.rendering;

import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.utils.FloatPair;

public final class CameraMovementSystem extends VoidEntitySystem {

  private float t;

  private float x0, x1, y0, y1;

  private final OrthographicCamera camera;

  private float T;

  public boolean active;

  public CameraMovementSystem(OrthographicCamera camera) {
    super();
    this.camera = camera;
    active = false;
  }

  public void move(int x1, int y1) {
    Vector3 position = camera.position;
    x0 = position.x;
    y0 = position.y;
    FloatPair p = MapTools.world2window(x1, y1);
    this.x1 = p.x;
    this.y1 = p.y;
    t = 0;
    MapPosition start = MapTools.libgdx2world(x0, y0);

    // d is used to calculate how long it will take to get to the target tile.
    // If it is close, d is small - if it is far, d is large
    // Very close by, d is similar to how many tiles away it is
    // For longer distances, it grows as sqrt(distance)
    float d = (float) Math.sqrt(MapTools.distance(start.x, start.y, x1, y1) + 0.25) - 0.5f;

    // start with a base of 0.4 seconds, then add d seconds every 4 tiles
    T = 0.4f + d / 4f;
    active = true;
  }

  public void process(float delta) {
    if (!active)
      return;

    float vx, vy;
    float Ax = 6 * (x1 - x0) / (T * T);
    float Ay = 6 * (y1 - y0) / (T * T);
    vx = Ax * (t) * (1 - t / T);
    vy = Ay * (t) * (1 - t / T);

    camera.translate(new Vector2(vx * delta, vy * delta));

    t += delta;
    if (t > T) {
      active = false;
    }
  }

  @Override
  protected void processSystem() {
    throw new IllegalStateException("Must call process(float)");
  }
}
