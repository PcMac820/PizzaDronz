package com.example.ilp_cw1.Definitions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NamedRegionAndPoint {

    @JsonProperty
    private LngLat position;
    @JsonProperty
    private NamedRegion namedRegion;

    public NamedRegionAndPoint(LngLat position, NamedRegion namedRegion) {
        this.position = position;
        this.namedRegion = namedRegion;
    }

    @Override
    public String toString() {
        return "{ \"position\": " + position.toString() +
                ", \"region\": " + namedRegion.toString() +
                " }";
    }

    public LngLat getPosition() {
        return position;
    }

    public NamedRegion getRegion() {
        return namedRegion;
    }
}


