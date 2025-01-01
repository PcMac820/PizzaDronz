package com.example.ilp_cw1.DTO;

public enum OrderValidationCode {
    UNDEFINED,                          // the reason code is undefined
    NO_ERROR,                           // no error present
    CARD_NUMBER_INVALID,                // card number is incorrect
    EXPIRY_DATE_INVALID,                // expiry date problems
    CVV_INVALID,                        // CVV is wrong
    TOTAL_INCORRECT,                    // order total is incorrect
    PIZZA_NOT_DEFINED,                  // a pizza in the order is undefined
    MAX_PIZZA_COUNT_EXCEEDED,           // too many pizzas ordered
    PIZZA_FROM_MULTIPLE_RESTAURANTS,    // pizzas were ordered from multiple restaurants
    RESTAURANT_CLOSED,                  // the restaurant is closed on the order day
    PRICE_FOR_PIZZA_INVALID,            // a pizza was ordered with an invalid price
    EMPTY_ORDER                         // no pizzas in order
}
