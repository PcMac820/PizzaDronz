package com.example.ilp_cw1.Definitions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    String orderNo;
    String orderDate;
    int priceTotalInPence;
    Pizza[] pizzasInOrder;
    CreditCardInformation creditCardInformation;

    public Order(){
    }

    public Order(String orderNo, String orderDate, int priceTotalInPence, Pizza[] pizzasInOrder,
                 CreditCardInformation creditCardInformation) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.priceTotalInPence = priceTotalInPence;
        this.pizzasInOrder = pizzasInOrder;
        this.creditCardInformation = creditCardInformation;
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

    public Pizza[] getPizzasInOrder() {
        return pizzasInOrder;
    }

    public void setPizzasInOrder(Pizza[] pizzasInOrder) {
        this.pizzasInOrder = pizzasInOrder;
    }

    public CreditCardInformation getCreditCardInformation() {
        return creditCardInformation;
    }

    public void setPriceTotalInPence(int i) { this.priceTotalInPence = i; }

    public void setCreditCardInformation(CreditCardInformation cardInfo) {
        this.creditCardInformation = cardInfo;
    }
}
