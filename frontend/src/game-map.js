////////////////////////////////////////////////////////////////////////////////
import {QuadArray} from './quad-array';

export class GameMap {
    constructor(width, height) {
        this.width = width;
        this.height = height;
        this.tiles = new QuadArray(width, height);
        this.tileTraversability = new QuadArray(width, height);
    }

    setTiles(startX, startY, tiles) {
        this.tiles.setArray(startX, startY, tiles);
    }

    setTileTraversability(startX, startY, tileTraversability) {
        this.tileTraversability.setArray(startX, startY, tileTraversability);
    }
    
    getTileIds(x, y) {
        return this.tiles.get(x, y);
    }

    getUniqueTileIds(startX, startY, width, height) {
        var tileIds = new Set();

        console.assert(startX + width <= this.width);
        console.assert(startY + height <= this.height);
        
        for (var y = startY; y < startY + height; y++) {
            for (var x = startX; x < startX + width; x++) {
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
