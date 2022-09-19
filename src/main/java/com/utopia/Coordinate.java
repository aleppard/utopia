package com.utpia;

// MYTODO point is zipper
/**
 * A class that defines a coordinate in a 2D space.
 */
public class Coordinate {
    public int x = 0;
    public int y = 0;

    public Coordinate() {};

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override public String toString() {
        return "(" + x + "," + y + ")";
    }
}
