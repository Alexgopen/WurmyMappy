package net.buddat.wgenerator;

import com.wurmonline.mesh.Tiles.Tile;

public class SeedTarJob extends SeedBiomeJob {

    private static final String[] biomeOptionTar = { "200", "1", "30", "70", "70", "70", "70", "0", "4000", "true", "30", "70", "true", "1" };
    
    public SeedTarJob(int waterHeight) {
        super(
            biomeOptionTar,
            Tile.TILE_TAR,
            0,
            0,
            waterHeight
        );
    }
}
