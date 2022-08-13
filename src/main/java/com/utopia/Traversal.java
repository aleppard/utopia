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

    // @todo We serialise this as a number (0/1) rather than a boolean
    // (true/false) to save space. The following annotation does not seem
    // to work for arrays:
    // @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    // A better solution is to move to bson.
    public short[][] hasSeen;

    public Traversal(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.hasSeen = new short[height][width];
    }
}
