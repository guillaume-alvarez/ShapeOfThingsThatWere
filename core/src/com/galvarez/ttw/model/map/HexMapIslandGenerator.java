package com.galvarez.ttw.model.map;

import static java.lang.Math.min;

import java.util.Properties;

import com.badlogic.gdx.math.MathUtils;

/**
 * Generates a map with more chances of an island on map center.
 * 
 * @author Guillaume Alvarez
 */
public class HexMapIslandGenerator implements MapGenerator {

  public static float coldThreshold = 0.1f;

  public static float hotThreshold = 0.65f;

  private static boolean isCold(float heat) {
    return heat < coldThreshold;
  }

  private static boolean isHot(float heat) {
    return heat > hotThreshold;
  }

  public static float dryThreshold = 0.3f;

  public static float wetThreshold = 0.7f;

  private static boolean isDry(float wet) {
    return wet < dryThreshold;
  }

  private static boolean isWet(float wet) {
    return wet > wetThreshold;
  }

  public static float deepWaterThreshold = 0.2f;

  public static float shallowWaterThreshold = 0.25f;

  public static float lowGroundsTreshold = 0.45f;

  public static float mediumGroundsThreshold = 0.65f;

  public static float highGroundThreshold = 0.88f;

  public static float veryHighGroundThreshold = 0.95f;

  @Override
  public Terrain[][] getMapData(Properties props) {
    int noise = Integer.parseInt(props.getProperty("noise"));
    int width = Integer.parseInt(props.getProperty("width"));
    int height = Integer.parseInt(props.getProperty("height"));

    MidpointDisplacement md = new MidpointDisplacement();

    float[][] heightMap = applyIslandForm(md.getMap2(noise, width, height));
    float[][] heatMap = southIsWarmer(md.getMap2(noise, width, height));
    float[][] wetMap = md.getMap2(noise, width, height);
    Terrain[][] returnMap = new Terrain[heightMap.length][heightMap[0].length];

    float mapHeight;
    boolean cold, hot, wet, dry;
    // Use the thresholds to fill in the return map
    for (int row = 0; row < heightMap.length; row++) {
      for (int col = 0; col < heightMap[row].length; col++) {
        mapHeight = heightMap[row][col];
        hot = isHot(heatMap[row][col]);
        cold = isCold(heatMap[row][col]);
        wet = isWet(wetMap[row][col]);
        dry = isDry(wetMap[row][col]);
        if (cold) {
          returnMap[row][col] = Terrain.ARCTIC;
        } else if (mapHeight < deepWaterThreshold) {
          returnMap[row][col] = Terrain.DEEP_WATER;
        } else if (mapHeight < shallowWaterThreshold) {
          returnMap[row][col] = Terrain.SHALLOW_WATER;
        } else if (mapHeight < lowGroundsTreshold) {
          // low level biomes
          if (hot && dry)
            returnMap[row][col] = Terrain.DESERT;
          else if (!cold && wet)
            returnMap[row][col] = Terrain.GRASSLAND;
          else if (wet)
            // TODO add JUNGLE when heat > hotThreshold
            returnMap[row][col] = Terrain.FOREST;
          else
            returnMap[row][col] = Terrain.PLAIN;
        } else if (mapHeight < mediumGroundsThreshold) {
          // slightly higher
          if (wet)
            returnMap[row][col] = Terrain.FOREST;
          else if (hot && dry)
            returnMap[row][col] = Terrain.DESERT;
          else
            returnMap[row][col] = Terrain.GRASSLAND;

        } else if (mapHeight < highGroundThreshold) {
          // hills
          if (wet && !hot)
            returnMap[row][col] = Terrain.FOREST;
          else
            returnMap[row][col] = Terrain.HILLS;
        } else if (mapHeight < veryHighGroundThreshold) {
          // mountains
          returnMap[row][col] = Terrain.MOUNTAIN;
        } else {
          // eternal snow
          returnMap[row][col] = Terrain.ARCTIC;
        }
      }
    }

    return returnMap;

  }

  private float[][] southIsWarmer(float[][] map) {
    int rows = map.length;
    int cols = map[0].length;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        float ratio = (float) (cols - col) / cols;
        map[row][col] += ratio * 2 * MathUtils.randomTriangular(1f, 1.5f);
      }
    }
    MidpointDisplacement.normalize(map);
    return map;
  }

  /**
   * Decrease height on borders and increase it on map center.
   */
  private float[][] applyIslandForm(float[][] heightMap) {
    int rows = heightMap.length;
    int cols = heightMap[0].length;
    int radius = min(rows / 2, cols / 2);
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        int distanceFromBorder = min(min(row, col), min(rows - row, cols - col));
        float ratio = (float) distanceFromBorder / radius;
        heightMap[row][col] *= ratio * 2 * MathUtils.randomTriangular(0.8f, 1.2f);
      }
    }
    MidpointDisplacement.normalize(heightMap);
    return heightMap;
  }

  @Override
  public Properties getDefaultValues() {
    Properties props = new Properties();
    props.setProperty("noise", "4");
    props.setProperty("width", "3");
    props.setProperty("height", "3");
    return props;
  }
}
