package com.utopia;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoadRegionTest {

    @Test
    public void test() {

        Region mapBounds = new Region(-64, -64, 128, 128);
        
        {
            Region region = LoadRegion.find(mapBounds,
                                            16,
                                            0,
                                            0,
                                            18,
                                            9);
            assertTrue(region.toString(),
                       region.equals(new Region(-16, -16, 48, 48)));
        }

        // Screen size bigger than map.
        {
            Region region = LoadRegion.find(mapBounds,
                                            16,
                                            0,
                                            0,
                                            1024,
                                            1024);
            assertTrue(region.toString(),
                       region.equals(new Region(-64, -64, 128, 128)));
        }
        
        // Top left corner.
        {
            Region region = LoadRegion.find(mapBounds,
                                            16,
                                            -64,
                                            -64,
                                            18,
                                            9);
            assertTrue(region.toString(),
                       region.equals(new Region(-64, -64, 64, 48)));
        }

        // Top right corner.
        {
            Region region = LoadRegion.find(mapBounds,
                                            16,
                                            63,
                                            -64,
                                            18,
                                            9);
            assertTrue(region.toString(),
                       region.equals(new Region(32, -64, 32, 48)));
        }

        // Bottom left corner.
        {
            Region region = LoadRegion.find(mapBounds,
                                            16,
                                            -64,
                                            63,
                                            18,
                                            9);
            assertTrue(region.toString(),
                       region.equals(new Region(-64, 32, 64, 32)));
        }

        // Bottom right corner.
        {
            Region region = LoadRegion.find(mapBounds,
                                            16,
                                            63,
                                            63,
                                            18,
                                            9);
            assertTrue(region.toString(),
                       region.equals(new Region(32, 32, 32, 32)));
        }
    }
}
    
