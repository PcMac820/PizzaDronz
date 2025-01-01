package com.example.ilp_cw1.DTO;

public class Pizza {

    private String name;
    private int priceInPence;

    public Pizza() {
    }

    public Pizza(String name, int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }

    public String getName() {
        return name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPriceInPence(int priceInPence) {
        this.priceInPence = priceInPence;
    }

}
