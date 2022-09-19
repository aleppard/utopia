package com.utopia;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @todo Rename to OutputMap.
 */
public class Map
{
    // @todo Use Region class here.
    public int startX = 0;
    public int startY = 0;
    public int width = 0;
    public int height = 0;
    public List<Integer>[][] tiles = null;

    // @todo We serialise this as a number (0/1) rather than a boolean
    // (true/false) to save space. The following annotation does not seem
    // to work for arrays:
    // @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    // A better solution is to move to bson.
    public short[][] isTraverseable;
    
    public Map(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.tiles = (ArrayList<Integer>[][])new ArrayList[height][width];
        this.isTraverseable = new short[height][width];
    }
}
