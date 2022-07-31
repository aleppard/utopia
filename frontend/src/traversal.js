////////////////////////////////////////////////////////////////////////////////
import {QuadArray} from './quad-array';

export class Traversal {
    constructor(width, height, hasSeen) {
        this.width = width
        this.height = height
        this.hasSeen = new QuadArray(width, height);
        this.hasSeen.setArray(0, 0, hasSeen);
    }

    hasSeenTile(x, y) {
        return this.hasSeen.get(x, y);
    }

    setTileSeen(x, y) {
        this.hasSeen.set(x, y, true);
    }
    
    updateTilesSeen(centreX, centreY) {
        var newTilesSeen = [];
        // @todo This could be made much faster!
        const RADIUS = 6;
        
        for (var x = centreX - RADIUS; x <= centreX + RADIUS; x++) {
            for (var y = centreY - RADIUS; y <= centreY + RADIUS; y++) {
                if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
                    if (!this.hasSeenTile(x, y)) {
                        if (Math.sqrt(Math.pow(centreX - x, 2) +
                                      Math.pow(centreY - y, 2)) < RADIUS) {
                            // @todo Use line of sight calculations.
                            this.setTileSeen(x, y);
                            newTilesSeen.push([x, y]);
                        }
                    }
                }
            }
        }
        
        return newTilesSeen  
    }
}
