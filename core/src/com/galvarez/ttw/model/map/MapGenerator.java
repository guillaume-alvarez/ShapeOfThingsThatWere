package com.galvarez.ttw.model.map;

import java.util.Properties;

public interface MapGenerator {
  Terrain[][] getMapData(Properties props);

  /**
   * Return the default values for all possible properties available to the
   * user.
   */
  Properties getDefaultValues();

  enum Generator {

    SIMPLE(new HexMapGenerator()), COMBINED(new HexMapCombinedGenerator()), ISLAND(new HexMapIslandGenerator());

    public final MapGenerator algo;

    private Generator(MapGenerator algo) {
      this.algo = algo;
    }

  }

}
