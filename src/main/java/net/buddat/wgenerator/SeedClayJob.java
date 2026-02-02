package net.buddat.wgenerator;

import com.wurmonline.mesh.Tiles.Tile;

public class SeedClayJob extends SeedBiomeJob {

    private static final String[] biomeOptionClay = { "500", "5", "30", "70", "70", "70", "70", "80", "80", "true", "30", "70", "true", "1" };
    
    public SeedClayJob(int waterHeight) {
        super(
            biomeOptionClay,
            Tile.TILE_CLAY,
            0,
            0,
            waterHeight
        );
    }
}
