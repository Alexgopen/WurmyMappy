package net.buddat.wgenerator;

import com.wurmonline.mesh.Tiles;

import net.buddat.wgenerator.util.ProgressHandler;

public class SandCoastlineJob implements TileMapJob {

    private final int minHeight;
    private final int maxHeight;

    public SandCoastlineJob(int minHeight, int maxHeight) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    @Override
    public String getName() {
        return "Sanding coastlines";
    }

    @Override
    public void run(TileMap map, ProgressHandler progress) {
        int size = map.getSize();
        int modified = 0;

        for (int x = 0; x < size; x++) {
            int pct = (int)((x / (double)size) * 100);
            progress.update(pct, "Sanding coastlines");

            for (int y = 0; y < size; y++) {
                int h = map.getSurfaceHeight(x, y);
                Tiles.Tile tile = map.getType(x, y);
                if (h >= minHeight && h <= maxHeight && tile.isGrass() || tile.isTree() || tile.isBush() || tile.id == Tiles.TILE_TYPE_DIRT) {
                    map.setType(x, y, Tiles.Tile.TILE_SAND);
                    modified++;
                }
            }
        }

        System.out.println("SandCoastlineJob modified " + modified + " tiles");
    }
}
