package com.example.ilp_cw1.unit;

import com.example.ilp_cw1.Controllers.OrderController;
import com.example.ilp_cw1.Definitions.*;
import com.example.ilp_cw1.Services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderController = new OrderController(restaurantService);
    }

    @Test
    public void testValidateOrder_WithNoErrors() {
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{new Pizza("R1: Margarita", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        List.of("FRIDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(OrderValidationCode.NO_ERROR, response.getBody().getOrderValidationCode());
    }

    @Test
    public void testValidateOrder_WithEmptyPizzaList() {
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        order.setPizzasInOrder(new Pizza[0]);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderValidationCode.EMPTY_ORDER, response.getBody().getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, response.getBody().getOrderStatus());
    }

    @Test
    public void testValidateOrder_WithMultipleRestaurants() {
        Order order = new Order("1", "2025-12-12", 2100,
                new Pizza[]{new Pizza("R1: Margarita", 1000), new Pizza("R2: Margarita", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))),

                new Restaurant("Restaurant2", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R2: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS, response.getBody().getOrderValidationCode());
    }

    @Test
    public void testValidateOrder_WithUndefinedPizza() {
        Order order = new Order("1", "2025-12-12", 2100,
                new Pizza[]{new Pizza("R1: Margarita", 1000), new Pizza("Hawaiian", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(OrderValidationCode.PIZZA_NOT_DEFINED, response.getBody().getOrderValidationCode());
    }

    @Test
    public void testValidateOrder_WithIncorrectPizzaPrice() {
        Order order = new Order("1", "2025-12-12", 1300,
                new Pizza[]{new Pizza("R1: Margarita", 1200)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(OrderValidationCode.PRICE_FOR_PIZZA_INVALID, response.getBody().getOrderValidationCode());
    }

    @Test
    public void testValidateOrder_WithOrderDateOnClosedDay() {
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{new Pizza("R1: Margarita", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "SATURDAY", "SUNDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(OrderValidationCode.RESTAURANT_CLOSED, response.getBody().getOrderValidationCode());
    }

    @Test
    public void testValidateOrder_WithMismatchingTotalPrice() {
        Order order = new Order("1", "2025-12-12", 1200,
                new Pizza[]{new Pizza("R1: Margarita", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(OrderValidationCode.TOTAL_INCORRECT, response.getBody().getOrderValidationCode());
    }

    @Test
    public void testValidateOrder_WithTooManyPizzas() {
        Order order = new Order("1", "2025-12-12", 5100,
                new Pizza[]{new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Margarita", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED, response.getBody().getOrderValidationCode());
    }

    @Test
    public void testValidateOrder_WithInvalidCVV() {
        String[] invalidCVVs = {null, "", "12", "1234", "ab3", " 123", "12a"};

        for (String cvv : invalidCVVs) {
            Order order = new Order("1", "2025-12-12", 1100,
                    new Pizza[]{new Pizza("R1: Margarita", 1000)},
                    new CreditCardInformation("1234567812345678", "12/25", cvv));

            List<Restaurant> mockRestaurants = List.of(
                    new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                            Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                            List.of(new Pizza("R1: Margarita", 1000))));

            when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

            ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
            assertEquals(OrderValidationCode.CVV_INVALID, response.getBody().getOrderValidationCode());
        }
    }

    @Test
    public void testValidateOrder_WithInvalidExpiryDate() {
        String[] invalidDates = {null, String.valueOf(YearMonth.from(LocalDate.now().minusMonths(1)))};

        for (String expiryDate : invalidDates) {
            Order order = new Order("1", "2025-12-12", 1100,
                    new Pizza[]{new Pizza("R1: Margarita", 1000)},
                    new CreditCardInformation("1234567812345678", expiryDate, "333"));

            List<Restaurant> mockRestaurants = List.of(
                    new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                            Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                            List.of(new Pizza("R1: Margarita", 1000))));

            when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

            ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
            assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, response.getBody().getOrderValidationCode());
        }
    }

    @Test
    public void testValidateOrder_WithInvalidCardNumber() {
        String[] invalidCardNumbers = {null, "", "123456781234567", "12345678123456789",
                "i234567812345678", " 1234567812345678", "123456781234567a"};

        for (String cardNumber : invalidCardNumbers) {
            Order order = new Order("1", "2025-12-12", 1100,
                    new Pizza[]{new Pizza("R1: Margarita", 1000)},
                    new CreditCardInformation(cardNumber, "12/25", "333"));

            List<Restaurant> mockRestaurants = List.of(
                    new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                            Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                            List.of(new Pizza("R1: Margarita", 1000))));

            when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

            ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
            assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, response.getBody().getOrderValidationCode());
        }
    }

    @Test
    public void testValidateOrder_WithNoMatchingRestaurant() {
        Order order = new Order("1", "2025-12-12", 1100,
                    new Pizza[]{new Pizza("R2: Hawaiian", 1000)},
                    new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);

        ResponseEntity<OrderValidationResult> response = orderController.validateOrder(order);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
