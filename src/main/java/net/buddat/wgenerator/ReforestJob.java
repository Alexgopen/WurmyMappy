package net.buddat.wgenerator;

import java.util.Random;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;

import net.buddat.wgenerator.util.ProgressHandler;

public class ReforestJob implements TileMapJob {

    private static final int SEA_LEVEL = 100;

    private final int targetFoliagePercent;
    private final Random rand = new Random();

    // --- Biome definitions ---
    private static final Tile[][] BIOMES = {
        { Tile.TILE_TREE_OAK, Tile.TILE_TREE_MAPLE, Tile.TILE_TREE_BIRCH },
        { Tile.TILE_TREE_PINE, Tile.TILE_TREE_FIR, Tile.TILE_TREE_CEDAR },
        { Tile.TILE_TREE_APPLE, Tile.TILE_TREE_CHERRY, Tile.TILE_TREE_LEMON },
        { Tile.TILE_TREE_WALNUT, Tile.TILE_TREE_CHESTNUT, Tile.TILE_TREE_LINDEN }
    };

    private static final int[] BIOME_RADII = { 20, 30, 15, 25 };
    private static final int BIOME_SEEDS_PER_MAP = 40;

    public ReforestJob(int targetFoliagePercent) {
        this.targetFoliagePercent = targetFoliagePercent;
    }

    @Override
    public String getName() {
        return "Foresting map";
    }

    @Override
    public void run(TileMap map, ProgressHandler progress) {
        final int size = map.getSize();

        float forestChance = targetFoliagePercent / 100f;

        int[][] forestPlan = new int[size][size];

        int[][][] biomeWeights = new int[BIOMES.length][size][size];

         // Big, overlapping blobs
        for (int b = 0; b < BIOMES.length; b++) {
            int radius = size / 8 + rand.nextInt(size / 16);
            int seeds = (size * size) / 2000;

            seedBiomeWeights(biomeWeights[b], size, radius, seeds);

            updateProgress(progress, 0, 30, b + 1, BIOMES.length, "Seeding biomes");
        }

    
         ensureBiomeCoverage(biomeWeights, size);
    
         // Planning
         for (int x = 0; x < size; x++) {
             progress.update((int)((x / (double)size) * 100), "Planning forest");
    
             for (int y = 0; y < size; y++) {
    
                 if (map.getType(x, y) != Tile.TILE_GRASS) continue;
                 if (map.getSurfaceHeight(x, y) < SEA_LEVEL) continue;
                 if (rand.nextFloat() > forestChance) continue;
    
                 int biome = pickBiomeWeighted(biomeWeights, x, y);
                 Tile tree = BIOMES[biome][rand.nextInt(BIOMES[biome].length)];
    
                 forestPlan[x][y] = tree.id;
             }
         }


        // -------------------------
        // Pass 4: Apply
        // -------------------------
        int placed = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int id = forestPlan[x][y];
                if (id != 0) {
                    map.setType(x, y, Tiles.getTile(id));
                    placed++;
                }
            }
        }

        System.out.println("ReforestJob placed " + placed + " trees");
    }

    private void updateProgress(
            ProgressHandler progress,
            int base,
            int span,
            int current,
            int total,
            String label) {

        int pct = base + (int)((current / (double)total) * span);
        progress.update(pct, label);
    }

    
    private void seedBiomeWeights(int[][] weights, int size, int radius, int seeds) {
        for (int i = 0; i < seeds; i++) {
            int cx = rand.nextInt(size);
            int cy = rand.nextInt(size);

            int r2 = radius * radius;

            for (int dx = -radius; dx <= radius; dx++) {
                int x = cx + dx;
                if (x < 0 || x >= size) continue;

                for (int dy = -radius; dy <= radius; dy++) {
                    int y = cy + dy;
                    if (y < 0 || y >= size) continue;

                    int d2 = dx * dx + dy * dy;
                    if (d2 <= r2) {
                        // Inverse falloff gives nice blobs
                        weights[x][y] += (r2 - d2);
                    }
                }
            }
        }
    }
    
    private int pickBiomeWeighted(int[][][] weights, int x, int y) {
        int total = 0;

        for (int b = 0; b < weights.length; b++) {
            total += weights[b][x][y];
        }

        if (total <= 0) return 0; // absolute fallback

        int roll = rand.nextInt(total);

        for (int b = 0; b < weights.length; b++) {
            roll -= weights[b][x][y];
            if (roll < 0) return b;
        }

        return 0;
    }


    private void ensureBiomeCoverage(int[][][] weights, int size) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int sum = 0;
                for (int b = 0; b < weights.length; b++) {
                    sum += weights[b][x][y];
                }
                if (sum == 0) {
                    // Force fallback biome
                    weights[0][x][y] = 1;
                }
            }
        }
    }
}
