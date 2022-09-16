////////////////////////////////////////////////////////////////////////////////

export const NORTH = "north"
export const EAST = "east"
export const SOUTH = "south"
export const WEST = "west"

/**
 * Given a start coordindate and an end coordinate, return a direction
 * we would need to travel to go from the start to the end coordinate.
 */
export function find(start, end) {
    // @todo Pick the largest difference.
    if (start[0] < end[0]) {
        return EAST;
    }
    else if (start[0] > end[0]) {
        return WEST;
    }
    else if (start[1] < end[1]) {
        return SOUTH;
    }
    else {
        return NORTH;
    }
}
