////////////////////////////////////////////////////////////////////////////////
export class GameMap {
    constructor(startX, startY, width, height, tiles, tileTraversability) {
        this.startX = startX
        this.startY = startY
        this.width = width
        this.height = height
        this.tiles = tiles
        this.tileTraversability = tileTraversability
    }

    getTileIds(x, y) {
        return this.tiles[y][x]
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
        return this.tileTraversability[y][x]
    }
}
