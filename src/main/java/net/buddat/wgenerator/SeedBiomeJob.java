package net.buddat.wgenerator;

import com.wurmonline.mesh.Tiles.Tile;
import net.buddat.wgenerator.util.ProgressHandler;

public class SeedBiomeJob implements TileMapJob {

    private final String[] options;
    private final Tile biomeTile;
    private final int flowerType;
    private final int flowerPercent;
    private final int waterHeight;

    public SeedBiomeJob(
        String[] options,
        Tile biomeTile,
        int flowerType,
        int flowerPercent,
        int waterHeight
    ) {
        this.options = options;
        this.biomeTile = biomeTile;
        this.flowerType = flowerType;
        this.flowerPercent = flowerPercent;
        this.waterHeight = waterHeight;
    }

    @Override
    public String getName() {
        return "Seeding " + biomeTile.getName();
    }

    @Override
    public void run(TileMap map, ProgressHandler progress) {

        int seedCount     = Integer.parseInt(options[0]);
        int biomeSize     = Integer.parseInt(options[1]);
        int maxSlope      = Integer.parseInt(options[2]);
        int growthN       = Integer.parseInt(options[3]);
        int growthS       = Integer.parseInt(options[4]);
        int growthE       = Integer.parseInt(options[5]);
        int growthW       = Integer.parseInt(options[6]);
        int minHeightOpt  = Integer.parseInt(options[7]);
        int maxHeightOpt  = Integer.parseInt(options[8]);
        boolean random    = Boolean.parseBoolean(options[9]);
        int growthMin     = Integer.parseInt(options[10]);
        int growthMax     = Integer.parseInt(options[11]);
        boolean aroundWater = Boolean.parseBoolean(options[12]);
        int density       = Math.max(1, Integer.parseInt(options[13]));

        int[] rates = new int[4];
        if (random) {
            if (growthMin >= growthMax) {
                growthMin = growthMax - 1;
            }
            rates[0] = growthMin;
            rates[1] = growthMax;
        } else {
            rates[0] = growthN;
            rates[1] = growthS;
            rates[2] = growthE;
            rates[3] = growthW;
        }

        int minHeight = aroundWater
            ? waterHeight - minHeightOpt
            : minHeightOpt;

        int maxHeight = aroundWater
            ? waterHeight + maxHeightOpt
            : maxHeightOpt;

        map.plantBiome(
            seedCount,
            biomeSize,
            density,
            rates,
            random,
            maxSlope,
            minHeight,
            maxHeight,
            biomeTile,
            flowerType,
            flowerPercent,
            progress
        );
    }
}
