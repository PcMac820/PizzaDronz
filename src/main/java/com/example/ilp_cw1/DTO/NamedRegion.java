package com.example.ilp_cw1.DTO;

import java.util.List;
import java.util.Objects;

public class NamedRegion {
    private String name;
    private List<LngLat> vertices;

    public NamedRegion() {}

    public NamedRegion(String name, List<LngLat> vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    //override for equals based on values rather than object
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NamedRegion that = (NamedRegion) obj;
        return Objects.equals(name, that.name) && Objects.equals(vertices, that.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vertices);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LngLat> getVertices() {
        return vertices;
    }
}
