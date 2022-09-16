package com.utopia;

import java.util.logging.Logger;

/**
 * Given the bounds of the map, the location of the user's avatar, and
 * the size (in tiles) of the user's screen, return the region of the map
 * that we should initially return to the client. This includes not only
 * enough of the map to fill the screen, but also bounds to allow the user
 * to travel while we load more of the map in the background.
 *
 * The region of the map is aligned so that it starts on a quad boundary
 * and is modulo the quad size. By forcing reading on quad boundaries we
 * can optimise loading of the map at a later stage.
 */
public class LoadRegion {

    private static final Logger LOGGER =
        Logger.getLogger(LoadRegion.class.getName());
    
    public static Region find(final Region mapBounds,
                              int quadSize,
                              int avatarX,
                              int avatarY,
                              int screenWidth,
                              int screenHeight) {
        Region region = new Region();

        // Check map is aligned to quad boundaries.
        Assert.assertEquals(mapBounds.startX % quadSize, 0);
        Assert.assertEquals(mapBounds.startY % quadSize, 0);        
        Assert.assertEquals(mapBounds.width % quadSize, 0);
        Assert.assertEquals(mapBounds.height % quadSize, 0);        
        
        // Check that avatar is within the map.
        Assert.assertTrue(mapBounds.contains(avatarX, avatarY));
        
        // Find the start of the region by centreing the screen around
        // the avatar and loading a quad size before and after the screen.
        region.startX = avatarX - screenWidth / 2 - quadSize;
        region.startY = avatarY - screenHeight / 2 - quadSize;

        // Make sure that the region does not start before the start of the map.
        region.startX = Math.max(mapBounds.startX, region.startX);
        region.startY = Math.max(mapBounds.startY, region.startY);
        
        // Shift the start of the region to the left so that it starts
        // on a quad boundary. If we are already at the start of the map this
        // should not do anything.
        final int shiftX = region.startX % quadSize;
        final int shiftY = region.startY % quadSize;
        region.startX -= shiftX;
        region.startY -= shiftY;
        
        // The region should be the screen size plus a quad size in each
        // direction plus whatever amount we had to shift it left earlier.
        // If the screen is at the start or end of the map this ends up being
        // two quads in one direction which is fine.
        region.width = screenWidth + 2 * quadSize + shiftX;
        region.width += (quadSize - region.width % quadSize);
        
        region.height = screenHeight + 2 * quadSize + shiftY;
        region.height += (quadSize - region.height % quadSize);        
        
        // Make sure the region doesn't exceed past the end of the map.
        if (region.getEndX() > mapBounds.getEndX()) {
            region.width = mapBounds.getEndX() - region.startX;
        }
        if (region.getEndY() > mapBounds.getEndY()) {
            region.height = mapBounds.getEndY() - region.startY;
        }

        // Check that our calculated start region is within the map bounds.
        Assert.assertTrue(region.toString(), mapBounds.contains(region));

        // Check that our calculated start region is aligned to
        // quad boundaries.
        Assert.assertEquals(region.startX % quadSize, 0);
        Assert.assertEquals(region.startY % quadSize, 0);        
        Assert.assertEquals(region.width % quadSize, 0);
        Assert.assertEquals(region.height % quadSize, 0); 
        
        // Check that the avatar is within the returned region.
        Assert.assertTrue(region.contains(avatarX, avatarY));
        
        return region;
    }
}
