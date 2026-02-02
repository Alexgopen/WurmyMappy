package net.buddat.wgenerator;

import com.wurmonline.mesh.Tiles;

import net.buddat.wgenerator.util.ProgressHandler;

public class SandCoastlineJob extends TileBetweenJob implements TileMapJob {

    public SandCoastlineJob(int waterHeight) {
        super(Tiles.Tile.TILE_SAND, -100, 100);
    }

    @Override
    public String getName() {
        return "Tundraing mountaintops";
    }
}
