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

    
    private static final float BIOME_RANDOM_CHANCE = 0.05f;

    private static final Tile[] ALL_TREES;

    static {
        int count = 0;
        for (Tile[] biome : BIOMES) {
            count += biome.length;
        }

        Tile[] tmp = new Tile[count];
        int i = 0;
        for (Tile[] biome : BIOMES) {
            for (Tile t : biome) {
                tmp[i++] = t;
            }
        }
        ALL_TREES = tmp;
    }
    
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
        boolean[][] biomeOccupied = new boolean[size][size];
        int[][] firstBiome = new int[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                firstBiome[x][y] = -1;
            }
        }
        int[][][] biomeWeights = new int[BIOMES.length][size][size];

         // Big, overlapping blobs
        for (int b = 0; b < BIOMES.length; b++) {
            int radius = size / 16 + rand.nextInt(size / 16);
            int seeds = (int) Math.pow(size/16, 2);

            seedBiomeWeights(
                    b,
                    biomeWeights[b],
                    firstBiome,
                    biomeOccupied,
                    size,
                    radius,
                    seeds
            );

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
    
                 Tile tree;

                 if (rand.nextFloat() < BIOME_RANDOM_CHANCE) {
                     // 5% chance: free-select from all trees
                     tree = ALL_TREES[rand.nextInt(ALL_TREES.length)];
                 } else {
                     // 95%: biome-respecting
                     int biome = pickBiomeDominant(biomeWeights, firstBiome, x, y);
                     Tile[] biomeTrees = BIOMES[biome];
                     tree = biomeTrees[rand.nextInt(biomeTrees.length)];
                 }

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

    
    private void seedBiomeWeights(
            int biomeIndex,
            int[][] weights,
            int[][] firstBiome,
            boolean[][] occupied,
            int size,
            int radius,
            int seeds) {

        int attempts = 0;
        int placed = 0;
        int maxAttempts = seeds * 10; // prevent infinite loops

        while (placed < seeds && attempts < maxAttempts) {
            attempts++;

            int cx = rand.nextInt(size);
            int cy = rand.nextInt(size);

            // Center must be free
            if (occupied[cx][cy]) {
                continue;
            }

            // Accept this center
            placed++;

            int r2 = radius * radius;

            for (int dx = -radius; dx <= radius; dx++) {
                int x = cx + dx;
                if (x < 0 || x >= size) continue;

                for (int dy = -radius; dy <= radius; dy++) {
                    int y = cy + dy;
                    if (y < 0 || y >= size) continue;

                    int d2 = dx * dx + dy * dy;
                    if (d2 <= r2) {
                        int w = (r2 - d2) + (r2 / 2);
                        weights[x][y] += w;

                        if (firstBiome[x][y] == -1) {
                            firstBiome[x][y] = biomeIndex;
                        }

                        occupied[x][y] = true;
                    }

                }
            }
        }
    }

    
    private int pickBiomeDominant(int[][][] weights, int[][] firstBiome, int x, int y) {

        int dominant = firstBiome[x][y];
        if (dominant < 0) return 0;

        int overlapping = 0;
        for (int b = 0; b < weights.length; b++) {
            if (b != dominant && weights[b][x][y] > 0) {
                overlapping++;
            }
        }

        float roll = rand.nextFloat();

        // 75% dominant
        if (roll < 0.75f) {
            return dominant;
        }

        // 5% free handled outside
        // Remaining 20% split across other biomes
        if (overlapping > 0) {
            float perBiome = 0.20f / overlapping;
            roll -= 0.75f;

            for (int b = 0; b < weights.length; b++) {
                if (b != dominant && weights[b][x][y] > 0) {
                    if (roll < perBiome) {
                        return b;
                    }
                    roll -= perBiome;
                }
            }
        }

        return dominant;
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
