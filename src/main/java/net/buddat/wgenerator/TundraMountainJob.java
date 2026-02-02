package net.buddat.wgenerator;

import com.wurmonline.mesh.Tiles;

import net.buddat.wgenerator.util.ProgressHandler;

public class TundraMountainJob extends TileBetweenJob implements TileMapJob {

    public TundraMountainJob() {
        super(Tiles.Tile.TILE_TUNDRA, 1650, 5000);
    }

    @Override
    public String getName() {
        return "Sanding coastlines";
    }
}
