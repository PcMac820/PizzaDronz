package com.example.ilp_cw1.DTO;

public class NextPosition {

    private LngLat start;
    private double angle;

    public NextPosition(LngLat start, double angle) {
        this.start = start;
        this.angle = angle;
    }

    public LngLat getStart() { return start; }

    public void setStart(LngLat start) {
        this.start = start;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}

