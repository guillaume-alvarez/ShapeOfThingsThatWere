package com.galvarez.ttw.model.map;

import com.galvarez.ttw.utils.MyMath;

public class HexMapGenerator {

  public static float deepWaterThreshold = 0.5f;

  public static float shallowWaterThreshold = 0.55f;

  public static float desertThreshold = 0.58f;

  public static float plainsThreshold = 0.62f;

  public static float grasslandThreshold = 0.7f;

  public static float forestThreshold = 0.8f;

  public static float hillsThreshold = 0.88f;

  public static float mountainsThreshold = 0.95f;

  public HexMapGenerator() {
  }

  public Terrain[][] getDiamondSquare(int noise, int width, int height) {
    int power = MyMath.pow(2, noise);
    MapTools.width = width * power + 1;
    MapTools.height = height * power + 1;

    MidpointDisplacement md = new MidpointDisplacement();

    float[][] map = md.getMap2(noise, width, height);
    Terrain[][] returnMap = new Terrain[map.length][map[0].length];
    // Use the thresholds to fill in the return map
    for (int row = 0; row < map.length; row++) {
      for (int col = 0; col < map[row].length; col++) {
        if (map[row][col] < deepWaterThreshold)
          returnMap[row][col] = Terrain.DEEP_WATER;
        else if (map[row][col] < shallowWaterThreshold)
          returnMap[row][col] = Terrain.SHALLOW_WATER;
        else if (map[row][col] < desertThreshold)
          returnMap[row][col] = Terrain.DESERT;
        else if (map[row][col] < plainsThreshold)
          returnMap[row][col] = Terrain.PLAIN;
        else if (map[row][col] < grasslandThreshold)
          returnMap[row][col] = Terrain.GRASSLAND;
        else if (map[row][col] < forestThreshold)
          returnMap[row][col] = Terrain.FOREST;
        else if (map[row][col] < hillsThreshold)
          returnMap[row][col] = Terrain.HILLS;
        else if (map[row][col] < mountainsThreshold)
          returnMap[row][col] = Terrain.MOUNTAIN;
        else
          returnMap[row][col] = Terrain.ARCTIC;
      }
    }

    return returnMap;
  }

}
