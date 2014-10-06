package com.galvarez.ttw.model.map;

import java.util.Properties;

import com.galvarez.ttw.utils.MyMath;

public class HexMapCombinedGenerator implements MapGenerator {

	public static float deepWaterThreshold = 0.5f;

	public static float shallowWaterThreshold = 0.55f;

	public static float lowGroundsTreshold = 0.65f;

	public static float mediumGroundsThreshold = 0.8f;

	public static float highGroundThreshold = 0.88f;
	public static float hotThreshold = 0.75f;
	public static float coldThreshold = 0.1f;

	public static float wetThreshold = 0.7f;
	public static float dryThreshold = 0.3f;

	@Override
	public Terrain[][] getMapData(Properties props) {
		int noise = Integer.parseInt(props.getProperty("noise"));
		int width = Integer.parseInt(props.getProperty("width"));
		int height = Integer.parseInt(props.getProperty("height"));

		int power = MyMath.pow(2, noise);
		MapTools.width = width * power + 1;
		MapTools.height = height * power + 1;

		MidpointDisplacement md = new MidpointDisplacement();

		float[][] heightMap = md.getMap2(noise, width, height);
		float[][] heatMap = md.getMap2(noise, width, height);
		float[][] wetMap = md.getMap2(noise, width, height);
		Terrain[][] returnMap = new Terrain[heightMap.length][heightMap[0].length];

		float mapHeight, heat, wet;
		// Use the thresholds to fill in the return map
		for (int row = 0; row < heightMap.length; row++) {
			for (int col = 0; col < heightMap[row].length; col++) {
				mapHeight = heightMap[row][col];
				heat = heatMap[row][col];
				wet = wetMap[row][col];
				if (heat < coldThreshold) {
					returnMap[row][col] = Terrain.ARCTIC;
				} else if (mapHeight < deepWaterThreshold) {
					returnMap[row][col] = Terrain.DEEP_WATER;
				} else if (mapHeight < shallowWaterThreshold) {
					returnMap[row][col] = Terrain.SHALLOW_WATER;
				} else if (mapHeight < lowGroundsTreshold) {
					// low level biomes
					if (heat > hotThreshold && wet < dryThreshold) {
						returnMap[row][col] = Terrain.DESERT;
					} else if (heat > coldThreshold && wet > wetThreshold) {
						returnMap[row][col] = Terrain.GRASSLAND;
					} else {
						returnMap[row][col] = Terrain.PLAIN;
					}
				} else if (mapHeight < mediumGroundsThreshold) {
					// slightly higher
					if (heat > hotThreshold && wet > wetThreshold) {
						returnMap[row][col] = Terrain.FOREST; // TODO add JUNGLE
					} else if (heat > hotThreshold && wet < dryThreshold) {
						returnMap[row][col] = Terrain.DESERT;
					} else if (heat > coldThreshold && wet > wetThreshold) {
						returnMap[row][col] = Terrain.FOREST;
					} else {
						returnMap[row][col] = Terrain.GRASSLAND;
					}

				} else if (mapHeight < highGroundThreshold) {
					// hills
					returnMap[row][col] = Terrain.HILLS;
				} else {
					// mountains
					returnMap[row][col] = Terrain.MOUNTAIN;
				}
			}
		}

		return returnMap;

	}

	@Override
	public Properties getDefaultValues() {
		Properties props = new Properties();
		props.setProperty("noise", "4");
		props.setProperty("width", "2");
		props.setProperty("height", "2");
		return props;
	}
}
