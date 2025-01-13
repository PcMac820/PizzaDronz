package com.example.ilp_cw1.DTO;

import java.util.Objects;

public class Pizza {

    private String name;
    private int priceInPence;

    public Pizza() {
    }

    public Pizza(String name, int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }

    //override for equals based on values rather than object
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pizza pizza = (Pizza) o;
        return priceInPence == pizza.priceInPence && Objects.equals(name, pizza.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, priceInPence);
    }

    public String getName() {
        return name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }

}
