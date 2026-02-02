package net.buddat.wgenerator;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;

import net.buddat.wgenerator.util.ProgressHandler;

public class TileBetweenJob implements TileMapJob {

    private final Tiles.Tile tile;
    private final int minHeight;
    private final int maxHeight;

    public TileBetweenJob(Tile tile, int minHeight, int maxHeight) {
        this.tile = tile;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    @Override
    public String getName() {
        return "Placing " + tile.getName() + " between heights "+minHeight + " and "+maxHeight;
    }

    @Override
    public void run(TileMap map, ProgressHandler progress) {
        int size = map.getSize();
        int modified = 0;

        for (int x = 0; x < size; x++) {
            int pct = (int)((x / (double)size) * 100);
            progress.update(pct, "Placing " + tile.getName() + " between heights " + minHeight + " and " + maxHeight);

            for (int y = 0; y < size; y++) {
                int h = map.getSurfaceHeight(x, y);
                Tiles.Tile tile = map.getType(x, y);
                if (h >= minHeight && h <= maxHeight && (tile.isGrass() || tile.isTree() || tile.isBush() 
                        || tile.id == Tiles.TILE_TYPE_DIRT || tile.id == Tiles.TILE_TYPE_SAND
                        || tile.id == Tiles.TILE_TYPE_CLAY || tile.id == Tiles.TILE_TYPE_TAR)) {
                    map.setType(x, y, this.tile);
                    modified++;
                }
            }
        }

        System.out.println("TileBetweenJob modified " + modified + " tiles");
    }
}
