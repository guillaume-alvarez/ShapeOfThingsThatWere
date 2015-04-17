package com.galvarez.ttw.model.map;

import com.badlogic.gdx.math.MathUtils;
import com.galvarez.ttw.utils.MyMath;

public class MidpointDisplacement {

  public float smoothness;

  public MidpointDisplacement() {
    // Smoothness controls how smooth the resultant terrain is.
    // Higher = smoother
    smoothness = 2f;
  }

  public float[][] getMap2(int n, int wmult, int hmult) {
    // get the dimensions of the map
    int power = MyMath.pow(2, n);
    int width = wmult * power + 1;
    int height = hmult * power + 1;

    // initialize arrays to hold values
    float[][] map = new float[width][height];

    int step = power / 2;
    float sum;
    int count;

    // h determines the fineness of the scale it is working on. After every
    // step, h is decreased by a factor of "smoothness"
    float h = 1;

    // Initialize the grid points
    for (int i = 0; i < width; i += 2 * step) {
      for (int j = 0; j < height; j += 2 * step) {
        map[i][j] = MathUtils.random(2 * h);
      }
    }

    // Do the rest of the magic
    while (step > 0) {
      // Diamond step
      for (int x = step; x < width; x += 2 * step) {
        for (int y = step; y < height; y += 2 * step) {
          sum = map[x - step][y - step] + // down-left
              map[x - step][y + step] + // up-left
              map[x + step][y - step] + // down-right
              map[x + step][y + step]; // up-right
          map[x][y] = sum / 4 + MathUtils.random(-h, h);
        }
      }

      // Square step
      for (int x = 0; x < width; x += step) {
        for (int y = step * (1 - (x / step) % 2); y < height; y += 2 * step) {
          sum = 0;
          count = 0;
          if (x - step >= 0) {
            sum += map[x - step][y];
            count++;
          }
          if (x + step < width) {
            sum += map[x + step][y];
            count++;
          }
          if (y - step >= 0) {
            sum += map[x][y - step];
            count++;
          }
          if (y + step < height) {
            sum += map[x][y + step];
            count++;
          }
          if (count > 0)
            map[x][y] = sum / count + MathUtils.random(-h, h);
          else
            map[x][y] = 0;
        }

      }
      h /= smoothness;
      step /= 2;
    }

    // Normalize the map
    normalize(map);

    return map;
  }

  /**
   * Modify the map values to have only values between 0.0 and 1.0 while keeping
   * the proportion between the different values.
   */
  public static void normalize(float[][] map) {
    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;
    for (float[] row : map) {
      for (float d : row) {
        if (d > max)
          max = d;
        if (d < min)
          min = d;
      }
    }
    for (int x = 0; x < map.length; x++) {
      for (int y = 0; y < map[0].length; y++) {
        map[x][y] = (map[x][y] - min) / (max - min);
      }
    }
  }
}
