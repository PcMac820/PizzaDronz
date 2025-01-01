package com.example.ilp_cw1.DTO;

public class NamedRegionAndPoint {

    private LngLat position;
    private NamedRegion namedRegion;

    public LngLat getPosition() {
        return position;
    }

    public void setPosition(LngLat position) {
        this.position = position;
    }

    public NamedRegion getRegion() {
        return namedRegion;
    }

    public void setRegion(NamedRegion namedRegion) {
        this.namedRegion = namedRegion;
    }
}


