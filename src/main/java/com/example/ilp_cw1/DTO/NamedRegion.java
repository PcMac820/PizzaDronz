package com.example.ilp_cw1.DTO;

import java.util.List;

public class NamedRegion {
    private String name;
    private List<LngLat> vertices;

    // Getters and setters for name and vertices
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LngLat> getVertices() {
        return vertices;
    }

    public void setVertices(List<LngLat> vertices) {
        this.vertices = vertices;
    }
}
