package com.utopia;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Map
{
    public int startX;
    public int startY;
    public int width;
    public int height;
    public List<Integer>[][] tiles;
    public boolean[][] isTraverseable;

    public Map(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.tiles = (ArrayList<Integer>[][])new ArrayList[height][width];
        this.isTraverseable = new boolean[height][width];
    }
}
