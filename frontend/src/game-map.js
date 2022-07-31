////////////////////////////////////////////////////////////////////////////////
import {QuadArray} from './quad-array';

export class GameMap {
    constructor(width, height, tiles, tileTraversability) {
        this.width = width;
        this.height = height;
        this.tiles = new QuadArray(width, height);
        this.tiles.setArray(0, 0, tiles);
        this.tileTraversability = new QuadArray(width, height);
        this.tileTraversability.setArray(0, 0, tileTraversability);
    }

    getTileIds(x, y) {
        return this.tiles.get(x, y);
    }

    getUniqueTileIds() {
        var tileIds = new Set();

        for (var y = 0; y < this.height; y++) {
            for (var x = 0; x < this.width; x++) {
                var singleTileIds = this.getTileIds(x, y)
                singleTileIds.forEach(tileId => {
                    tileIds.add(tileId);
                });
            }
        }
        
        return Array.from(tileIds);
    }
    
    isTileTraverseable(x, y) {
        return this.tileTraversability.get(x, y);
    }
}
