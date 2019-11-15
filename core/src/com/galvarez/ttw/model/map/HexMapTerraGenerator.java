package com.galvarez.ttw.model.map;

import java.util.Properties;

public class HexMapTerraGenerator implements MapGenerator {

    private final HexMapIslandGenerator island;

    public HexMapTerraGenerator() {
        this.island = new HexMapIslandGenerator();
    }

    @Override
    public Terrain[][] getMapData(Properties props) {
        Terrain[][] continent1 = island.getMapData(props);
        Terrain[][] continent2 = island.getMapData(props);
        Terrain[][] map = new Terrain[continent1.length + continent2.length][continent1[0].length];
        System.arraycopy(continent1, 0, map, 0, continent1.length);
        System.arraycopy(continent2, 0, map, continent1.length, continent2.length);
        return map;
    }

    @Override
    public Properties getDefaultValues() {
        return island.getDefaultValues();
    }
}
