////////////////////////////////////////////////////////////////////////////////

// The size of the quadrants that the array is broken up into.
// This should be a power-of-2 for maximum performance.
const QUAD_SIZE = 20;

export class QuadArray {
    constructor(width, height) {
        console.assert(width % QUAD_SIZE == 0);
        console.assert(height % QUAD_SIZE == 0);

        this.width = width
        this.height = height

        this.quads = create2DArray(width / QUAD_SIZE, height / QUAD_SIZE);
    }

    has(x, y) {
        if (this.quads[Math.floor(y / QUAD_SIZE),
                       Math.floor(x / QUAD_SIZE)]) {
            return true;
        }
        else {
            return false;
        }
    }

    get(x, y) {
        var row = Math.floor(y / QUAD_SIZE);
        var column = Math.floor(x / QUAD_SIZE);
        var quad = this.quads[row][column];
        
        if (quad == null) {
            return null;
        }

        return quad[y % QUAD_SIZE][x % QUAD_SIZE];
    }

    set(x, y, value) {
        var row = Math.floor(y / QUAD_SIZE);
        var column = Math.floor(x / QUAD_SIZE);
        var quad = this.quads[row][column];
        
        if (quad == null) {
            return null;
        }

        quad[y % QUAD_SIZE][x % QUAD_SIZE] = value;
    }
   
    setArray(startX, startY, array) {
        console.assert(startX % QUAD_SIZE == 0);
        console.assert(startY % QUAD_SIZE == 0);

        var height = array.length;
        var width = array[0].length;
        console.assert(height % QUAD_SIZE == 0);
        console.assert(width % QUAD_SIZE == 0);
        
        var rowOffset = startX / QUAD_SIZE;
        var columnOffset = startY / QUAD_SIZE;

        // Iterate over each quad in the array.
        for (var y = 0, row = 0; y < height; y += QUAD_SIZE, row++) {
            for (var x = 0, column = 0; x < width; x += QUAD_SIZE, column++) {
                var quad = slice2DArray(array, x, y, QUAD_SIZE, QUAD_SIZE);
                this.quads[rowOffset + row][columnOffset + column] = quad;
            }
        }
    }
}

// @todo Create 2D array class.
function create2DArray(width, height) {
    var array = [];
    
    for (var y = 0; y < height; y++) {
        var row = [];
        for (var x = 0; x < width; x++) {
            row[x] = null;
        }
        array[y] = row;
    }

    return array;
}

function slice2DArray(oldArray, startX, startY, width, height) {
    var newArray = []
    
    for (var y = 0; y < height; y++) {
        newArray[y] = oldArray[startY + y].slice(startX, startX + width);
    }

    return newArray;
}
