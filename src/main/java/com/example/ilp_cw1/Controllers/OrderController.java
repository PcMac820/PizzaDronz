package com.example.ilp_cw1.Controllers;

import com.example.ilp_cw1.DTO.*;
import com.example.ilp_cw1.Services.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
public class OrderController {

    private final RestaurantService restaurantService;

    @Autowired
    public OrderController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping("/validateOrder")
    public ResponseEntity<OrderValidationResult> validateOrder(@RequestBody Order order) {
        try{
            OrderValidationResult thisOrderValidation = new OrderValidationResult(OrderStatus.UNDEFINED, OrderValidationCode.UNDEFINED);

            String orderCardNo = order.getCreditCardInformation().getCreditCardNumber();
            String orderCVV = order.getCreditCardInformation().getCvv();

            String orderCardExpiryString = order.getCreditCardInformation().getCreditCardExpiry();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth thisMonth = YearMonth.from(LocalDate.now());

            Pizza[] orderPizzas = order.getPizzasInOrder();

            // Check if empty
            if (orderPizzas.length == 0) {
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.EMPTY_ORDER);
                return new ResponseEntity<>(thisOrderValidation, HttpStatus.BAD_REQUEST);
            }

            String restaurantNo = orderPizzas[0].getName().substring(0, 2);

            Optional<Restaurant> optionalRestaurant = restaurantService.fetchRestaurants().stream()
                    .filter(restaurant -> restaurant.getMenu().get(0).getName().substring(0, 2)
                            .equals(restaurantNo)).findFirst();
            if (optionalRestaurant.isEmpty()) {
                throw new IllegalArgumentException("No matching restaurant found for the order.");
            }
            Restaurant orderRestaurant = optionalRestaurant.get();

            List<DayOfWeek> openDays = orderRestaurant.getOpeningDays().stream()
                    .map(DayOfWeek::valueOf).toList();
            DayOfWeek orderDay = LocalDate.parse(order.getOrderDate()).getDayOfWeek();

            int orderTotal = order.getPriceTotalInPence();
            int calculatedTotal = 100; // 100 for order fee
            boolean pizzaUndefined = false;

            boolean differentRestaurants = false;

            boolean badPizzaPrice = false;

            for (Pizza pizza : orderPizzas){
                calculatedTotal += pizza.getPriceInPence();

                // For Pizza Definition Checking
                if (pizza.getName() == null || pizza.getName().isEmpty() || pizza.getPriceInPence() == 0){
                    pizzaUndefined = true;
                }

                // For checking same restaurant
                if (!pizza.getName().substring(0, 2).equals(restaurantNo)) {
                    differentRestaurants = true;
                }

                // For checking pizza prices
                if (pizza.getPriceInPence() < 0) {
                    badPizzaPrice = true;
                }
            }

            if (badPizzaPrice) {
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.PRICE_FOR_PIZZA_INVALID);
            }

            // Multiple Restaurants Validation Check
            else if (differentRestaurants){
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
            }

            // Restaurant Opening Days Validation Check
            else if (!openDays.contains(orderDay)) {
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.RESTAURANT_CLOSED);
            }

            // Pizza Count Validation Check
            else if (orderPizzas.length > 4){
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
            }

            // Pizza Definition Validation Check
            else if (pizzaUndefined) {
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
            }

            // Total is Correct Check
            else if (orderTotal <= 0 || orderTotal != calculatedTotal){
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.TOTAL_INCORRECT);
            }

            // CVV Validation Check
            else if (orderCVV == null || orderCVV.length() != 3) {
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.CVV_INVALID);
            }

            // Expiry Date Validation Check
            else if (orderCardExpiryString == null ||
                    orderCardExpiryString.isEmpty() ||
                    YearMonth.parse(orderCardExpiryString, formatter).isBefore(thisMonth) ||
                    YearMonth.parse(orderCardExpiryString, formatter).equals(thisMonth)) {
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            }

            // Card Number Validation Check
            else if (orderCardNo == null || orderCardNo.length() != 16) {
                thisOrderValidation.setOrderStatus(OrderStatus.INVALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.CARD_NUMBER_INVALID);
            }

            // No error found
            else {
                thisOrderValidation.setOrderStatus(OrderStatus.VALID);
                thisOrderValidation.setOrderValidationCode(OrderValidationCode.NO_ERROR);
            }

            if (thisOrderValidation.getOrderStatus().equals(OrderStatus.VALID)) {
                return new ResponseEntity<>(thisOrderValidation, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(thisOrderValidation, HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
