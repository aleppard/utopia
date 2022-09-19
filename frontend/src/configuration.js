////////////////////////////////////////////////////////////////////////////////

// Frame rate when moving avatar.
// @todo Rename to FRAME_RATE when we animate the background image.
export const MOVE_FRAME_COUNT_PER_SECOND = 60;

// Number of pixels to move avatar per frame. Should be a divisor of
// the tile size.
export const MOVE_PIXEL_COUNT = 4;

// Width and height of tile in pixels.
export const TILE_SIZE = 32;

// We batch tiles together in 2D batches called quads. We load tiles as
// quads rather than loading them individually. The quad size must
// be a multiple of the map width and map start coordinates.
export const QUAD_SIZE = 10;
