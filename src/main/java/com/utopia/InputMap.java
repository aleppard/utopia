package com.utopia;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class InputMap
{
    // @todo Use Region class here.

    @JsonProperty
    public int startX = 0;

    @JsonProperty
    public int startY = 0;

    @JsonProperty
    public int width = 0;

    @JsonProperty
    public int height = 0;

    @JsonProperty
    public List<String>[][] tiles = null;

    public InputMap() {};
    
    public InputMap(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.tiles = (ArrayList<String>[][])new ArrayList[height][width];
    }
}
