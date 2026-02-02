package net.buddat.wgenerator;

import net.buddat.wgenerator.util.ProgressHandler;

interface TileMapJob {
    String getName();
    void run(TileMap map, ProgressHandler progress);
}

