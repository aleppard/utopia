////////////////////////////////////////////////////////////////////////////////

/**
 * A class that defines a rectangular region in a larger space.
 */
export class Region {
    constructor(startX = 0, startY = 0, width = 0, height = 0) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
    }

    get endX() {
        return this.startX + this.width;
    }

    get endY() {
        return this.startY + this.height;
    }

    // MYTODO minimum here and in java shouldnt' be inclusive? THIS but
    // not region. update java.
    contains(x, y) {
        return (x >= this.startX &&
                y >= this.startY &&
                x < this.endX &&
                y < this.endY);
    }

    containsRegion(other) {
        return (other.startX >= this.startX &&
                other.startY >= this.startY &&
                other.endX <= this.endX &&
                other.endY <= this.endY);
    }
}
