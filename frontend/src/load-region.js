////////////////////////////////////////////////////////////////////////////////
import {Region} from './region';

// MYTODO DOC copy from startregion
export function calculate(mapBounds,
                          quadSize,
                          avatarX,
                          avatarY,
                          screenWidth,
                          screenHeight) {
    let region = new Region();

    // Check map is aligned to quad boundaries.
    console.assert(mapBounds.startX % quadSize === 0,
                   `map startX ${mapBounds.startX} not modulo quad.`);
    console.assert(mapBounds.startY % quadSize === 0,
                   `map startY ${mapBounds.startT} not modulo quad.`);
    console.assert(mapBounds.width % quadSize === 0,
                   `map width ${mapBounds.width} not modulo quad.`);
    console.assert(mapBounds.height % quadSize === 0,
                   `map height ${mapBounds.height} not modulo quad.`);        
    
    // Check that avatar is within the map.
    console.assert(mapBounds.contains(avatarX, avatarY),
                   `map ${JSON.stringify(mapBounds)} does not contain avatar ${avatarX},${avatarY}.`);
    
    // Find the start of the region by centreing the screen around
    // the avatar and loading a quad size before and after the screen.
    region.startX = avatarX - Math.ceil(screenWidth / 2) - quadSize;
    region.startY = avatarY - Math.ceil(screenHeight / 2) - quadSize;
    
    // Make sure that the region does not start before the start of the map.
    region.startX = Math.max(mapBounds.startX, region.startX);
    region.startY = Math.max(mapBounds.startY, region.startY);
    
    // Shift the start of the region to the left so that it starts
    // on a quad boundary. If we are already at the start of the map this
    // should not do anything.
    const shiftX = region.startX % quadSize;
    const shiftY = region.startY % quadSize;
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
    if (region.endX > mapBounds.endX) {
        region.width = mapBounds.endX - region.startX;
    }
    if (region.endY > mapBounds.endY) {
        region.height = mapBounds.endY - region.startY;
    }
    
    // Check that our calculated start region is within the map bounds.
    console.assert(mapBounds.containsRegion(region),
                   `map ${JSON.stringify(mapBounds)} does not contain region ${JSON.stringify(region)}.`);
    
    // Check that our calculated start region is aligned to
    // quad boundaries.
    console.assert(region.startX % quadSize === 0,
                   `region startX ${region.startX} not modulo quad.`);
    console.assert(region.startY % quadSize === 0,
                   `region startY ${region.startY} not modulo quad.`);
    console.assert(region.width % quadSize === 0,
                   `region width ${region.width} not modulo quad.`);
    console.assert(region.height % quadSize === 0,
                   `region height ${region.height} not modulo quad.`);    
    
    // Check that the avatar is within the returned region.
    console.assert(region.contains(avatarX, avatarY),
                   `region ${JSON.stringify(region)} does not contain avatar ${avatarX},${avatarY}.`);
    
    return region;
}
