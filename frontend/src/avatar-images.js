////////////////////////////////////////////////////////////////////////////////

import {TILE_SIZE} from './configuration';
import * as Direction from './direction';
import {Region} from './region';

const AVATAR_IMAGE_URL = '/api/v0/avatar.png';

const AVATAR_SOUTH_FACING_ROW_NUMBER = 0;
const AVATAR_WEST_FACING_ROW_NUMBER = 1;
const AVATAR_EAST_FACING_ROW_NUMBER = 2;
const AVATAR_NORTH_FACING_ROW_NUMBER = 3;

export class AvatarImages {
    constructor() {
        this.images = new Map();
    }

    load(avatarId) {
        return new Promise((resolve, reject) => {
            const image = new Image();
            image.addEventListener('load', () => {
                this.images.set(avatarId, image);
                resolve(image);
            });
            image.addEventListener('error', (error) => reject(error));
            image.src = AVATAR_IMAGE_URL + "?id=" + avatarId;
        });
    }

    /**
     * Convert a direction where the avatar is facing to
     * the row in the composite image that has the avatar image facing
     * that direction.
     */
    directionToRowNumber(direction) {
        if (direction === Direction.NORTH) {
            return AVATAR_NORTH_FACING_ROW_NUMBER;
        }
        else if (direction === Direction.WEST) {
            return AVATAR_WEST_FACING_ROW_NUMBER;
        }
        else if (direction === Direction.SOUTH) {
            return AVATAR_SOUTH_FACING_ROW_NUMBER;            
        }
        else {
            return AVATAR_EAST_FACING_ROW_NUMBER;                        
        }
    }

    /**
     * Return avatar composite image. Call getRegion() to find the region
     * in the avatar composite image to use.
     */
    get(avatarId) {
        return this.images.get(avatarId);
    }

    /**
     * Return the region in the avatar image that corresponds to the avatar
     * facing in the given direction.
     */
    getRegion(direction) {
        const rowNumber = this.directionToRowNumber(direction);

        // @todo There are 3 frames for each direction for animation.
        const columnNumber = 1;

        return new Region(columnNumber * TILE_SIZE,
                          rowNumber * TILE_SIZE,
                          TILE_SIZE,
                          TILE_SIZE);
    }
}
