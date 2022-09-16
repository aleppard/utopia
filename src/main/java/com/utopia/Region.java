package com.utopia;

/**
 * A class that defines a rectangular region in a larger space.
 */
public class Region {
    public int startX = 0;
    public int startY = 0;
    public int width = 0;
    public int height = 0;

    public Region() {}
    
    public Region(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
    }

    public int getEndX() {
        return startX + width;
    }

    public int getEndY() {
        return startY + height;
    }

    public boolean contains(int x, int y) {
        return (x >= startX &&
                y >= startY &&
                x <= getEndX() &&
                y <= getEndY());
    }

    public boolean contains(final Region other) {
        return (other.startX >= startX &&
                other.startY >= startY &&
                other.getEndX() <= getEndX() &&
                other.getEndY() <= getEndY());
    }

    @Override public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }

        final Region otherRegion = (Region)other;
        return (this.startX == otherRegion.startX &&
                this.startY == otherRegion.startY &&
                this.width == otherRegion.width &&
                this.height == otherRegion.height);
    }
    
    @Override public String toString() {
        return "(" + startX + "," + startY + "," + width + "," + height + ")";
    }
}
