package com.galvarez.ttw.rendering;

import static java.lang.Math.min;

import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.utils.FloatPair;

public final class CameraMovementSystem extends VoidEntitySystem {

  private float t;

  /** Camera position start and destination. */
  private float x0, x1, y0, y1;

  private float Ax, Ay;

  private final OrthographicCamera camera;

  private float T;

  public boolean active;

  public CameraMovementSystem(OrthographicCamera camera) {
    super();
    this.camera = camera;
    active = false;
  }

  public void move(int xWorld, int yWorld) {
    Vector3 position = camera.position;
    x0 = position.x;
    y0 = position.y;
    FloatPair p = MapTools.world2window(xWorld, yWorld);
    this.x1 = p.x;
    this.y1 = p.y;
    t = 0;
    MapPosition startWorld = MapTools.libgdx2world(x0, y0);

    // d is used to calculate how long it will take to get to the target tile.
    // If it is close, d is small - if it is far, d is large
    // Very close by, d is similar to how many tiles away it is
    // For longer distances, it grows as sqrt(distance)
    float d = (float) Math.sqrt(MapTools.distance(startWorld.x, startWorld.y, xWorld, yWorld) + 0.25) - 0.5f;

    // start with a base of 0.4 seconds, then add d seconds every 4 tiles
    T = 0.4f + d / 4f;
    Ax = (x1 - x0) / T;
    Ay = (y1 - y0) / T;
    active = true;
  }

  @Override
  protected void processSystem() {
    if (!active)
      return;

    t = min(t + world.getDelta(), T);
    // TODO increase speed from start, reduce when approaching end, a curb like:
    // http://1.bp.blogspot.com/-ps-1MpKTgbc/UXTSzyOGhKI/AAAAAAAAAGc/9Kef1mD-Plk/s1600/camera.png
    // maybe use this formula:
    // http://www.wolframalpha.com/input/?i=x+%2F+sqrt%281+%2B+x^2%29
    camera.position.set(x0 + Ax * t, y0 + Ay * t, 0);

    if (t >= T) {
      active = false;
    }
  }
}
