package com.utopia;

/**
 *
 */
public class Traversal
{
    public int startX;
    public int startY;
    public int width;
    public int height;
    public boolean[][] hasSeen;

    public Traversal(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.hasSeen = new boolean[height][width];
    }
}
