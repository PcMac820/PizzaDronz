package com.example.ilp_cw1.DTO;

public class NextPosition {

    LngLat start;
    double angle;

    public NextPosition(LngLat start, double angle) {
        this.start = start;
        this.angle = angle;
    }

    public LngLat getStart() { return start; }

    public double getAngle() {
        return angle;
    }
}

