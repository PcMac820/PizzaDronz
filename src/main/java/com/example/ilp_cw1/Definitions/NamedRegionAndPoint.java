package com.example.ilp_cw1.Definitions;

public class NamedRegionAndPoint {

    LngLat position;
    NamedRegion namedRegion;

    public NamedRegionAndPoint(LngLat position, NamedRegion namedRegion) {
        this.position = position;
        this.namedRegion = namedRegion;
    }

    public LngLat getPosition() {
        return position;
    }

    public NamedRegion getRegion() {
        return namedRegion;
    }
}


