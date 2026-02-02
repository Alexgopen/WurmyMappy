package com.wurmonline.wurmapi.api;

import java.io.File;
import java.io.IOException;

public class WurmAPIV2 {
    
    /**
     * Creates new WurmAPI instance. This method must be used on existing and valid world directory.
     * 
     * @param worldDirectory path to existing world directory.
     * @return WurmAPI instance
     */
    public static WurmAPIV2 open(String worldDirectory) throws IOException {
        return new WurmAPIV2(worldDirectory);
    }
    
    /**
     * Creates new WurmAPI instance.
     * 
     * @param worldDirectory path to new or existing world directory.
     * @param powerOfTwo power of two of new map (must be between 10 and 15)
     * @return WurmAPI instance
     */
    public static WurmAPIV2 create(String worldDirectory, int powerOfTwo) throws IOException {
        return new WurmAPIV2(worldDirectory, powerOfTwo);
    }
    
    private final String rootDir;
    private final MapDataV2 mapData;
    
    private WurmAPIV2(String worldDirectory) throws IOException {
        this.rootDir = worldDirectory + File.separator;
        File file = new File(rootDir);
        file.mkdirs();
        
        this.mapData = new MapDataV2(rootDir);
    }
    
    private WurmAPIV2(String worldDirectory, int powerOfTwo) throws IOException {
        if (powerOfTwo < 9 || powerOfTwo > 15) {
            throw new IllegalArgumentException("Invalid map size: map with size 2^" + powerOfTwo + " cannot be created");
        }
        
        this.rootDir = worldDirectory + File.separator;
        File file = new File(rootDir);
        file.mkdirs();
        
        this.mapData = new MapDataV2(rootDir, powerOfTwo);
    }
    
    public MapDataV2 getMapData() {
        return mapData;
    }
    
    /**
     * Releases all native resources used by WurmAPI. It shouldn't be used after calling this method.
     */
    public void close() {
        mapData.close();
    }
    
}
