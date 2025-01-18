package com.example.ilp_cw1.Definitions;

import java.util.Objects;

public class LngLat {

    private Double lng;
    private Double lat;

    public LngLat() {
    }

    public LngLat(Double lng, Double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "{ \"lng\": " + this.lng + ", \"lat\": " + this.lat + " }";
    }

    //override for equals based on values rather than object
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LngLat that = (LngLat) obj;
        return Objects.equals(lng, that.lng) && Objects.equals(lat, that.lat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lng, lat);
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
