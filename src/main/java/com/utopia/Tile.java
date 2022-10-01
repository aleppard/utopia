package com.utopia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tile {
    @JsonProperty
    public boolean isTraverseable;

    @JsonProperty
    public String northConnectorCode;

    @JsonProperty
    public String eastConnectorCode;

    @JsonProperty
    public String southConnectorCode;

    @JsonProperty
    public String westConnectorCode;            
    
    @JsonProperty
    public String code;

    @JsonProperty
    public String description;
}
