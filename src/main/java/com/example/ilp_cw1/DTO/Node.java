package com.example.ilp_cw1.DTO;

import java.util.Objects;

public class Node implements Comparable<Node> {
    LngLat position;
    Node parent;
    public double gCost;
    double hCost;
    double fCost;

    public Node(LngLat position, Node parent, double gCost, double hCost) {
        this.position = position;
        this.parent = parent;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
    }

    public double getGCost() {
        return gCost;
    }

    public double getHCost() {
        return hCost;
    }

    public double getFCost() {
        return fCost;
    }

    public LngLat getPosition() {
        return position;
    }

    public Node getParent() {
        return parent;
    }

    public void setGCost(double gCost) {
        this.gCost = gCost;
    }

    public void setHCost(double hCost) {
        this.hCost = hCost;
    }

    public void setFCost(double fCost) {
        this.fCost = fCost;
    }

    public int compareTo(Node other) {
        return Double.compare(this.fCost, other.fCost);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return position.equals(node.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
