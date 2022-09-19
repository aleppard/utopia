////////////////////////////////////////////////////////////////////////////////

const TILE_IMAGE_URL = '/api/v0/tile.png';

export class TileImages {
    constructor() {
        this.images = new Map();
    }

    load(tileId) {
        return new Promise((resolve, reject) => {
            const image = new Image();
            image.addEventListener('load', () => {
                this.images.set(tileId, image);
                resolve(image);
            });
            image.addEventListener('error', (error) => reject(error));
            image.src = TILE_IMAGE_URL + "?id=" + tileId;
        });
    }

    loadAll(tileIds) {
        // @todo It would be good if we could bundle up tile load requests
        // rather than performing a single request per tile.
        return tileIds.map((tileId) => {
            return this.load(tileId);
        });
    }

    /**
     * Return tile image.
     */
    get(tileId) {
        return this.images.get(tileId);
    }
}
