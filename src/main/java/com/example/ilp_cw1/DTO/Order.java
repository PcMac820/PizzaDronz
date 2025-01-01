package com.example.ilp_cw1.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    private String orderNo;
    private String orderDate;
    private int priceTotalInPence;
    private Pizza[] pizzasInOrder;
    private CreditCardInformation creditCardInformation;

    public Order(String orderNo, String orderDate, int priceTotalInPence, Pizza[] pizzasInOrder, CreditCardInformation creditCardInformation) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.priceTotalInPence = priceTotalInPence;
        this.pizzasInOrder = pizzasInOrder;
        this.creditCardInformation = creditCardInformation;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getPriceTotalInPence() {
        return priceTotalInPence;
    }

    public void setPriceTotalInPence(int priceTotalInPence) {
        this.priceTotalInPence = priceTotalInPence;
    }

    public Pizza[] getPizzasInOrder() {
        return pizzasInOrder;
    }

    public void setPizzasInOrder(Pizza[] pizzasInOrder) {
        this.pizzasInOrder = pizzasInOrder;
    }

    public CreditCardInformation getCreditCardInformation() {
        return creditCardInformation;
    }

    public void setCreditCardInformation(CreditCardInformation creditCardInformation) {
        this.creditCardInformation = creditCardInformation;
    }

    public int calculateTotalPrice() {
        int total = 0;
        for (Pizza pizza : pizzasInOrder) {
            total += pizza.getPriceInPence();
        }
        return total;
    }

}
