package com.example.ilp_cw1.DTO;

import java.util.List;
import java.util.Objects;

public class Restaurant {

    private String name;
    private LngLat location;
    private List<String> openingDays;
    private List<Pizza> menu;

    public Restaurant() {}

    public Restaurant(String name, LngLat location, List<String> openingDays, List<Pizza> menu) {
        this.name = name;
        this.location = location;
        this.openingDays = openingDays;
        this.menu = menu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(location, that.location) &&
                Objects.equals(openingDays, that.openingDays) &&
                Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, openingDays, menu);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LngLat getLocation() {
        return location;
    }

    public void setLocation(LngLat location) {
        this.location = location;
    }

    public List<String> getOpeningDays() {
        return openingDays;
    }

    public void setOpeningDays(List<String> openingDays) {
        this.openingDays = openingDays;
    }

    public List<Pizza> getMenu() {
        return menu;
    }

    public void setMenu(List<Pizza> menu) {
        this.menu = menu;
    }
}
