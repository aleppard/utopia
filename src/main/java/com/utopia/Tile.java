package com.utopia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tile {
    @JsonProperty
    public boolean isTraverseable;

    @JsonProperty
    public String code;

    @JsonProperty
    public String description;
}
