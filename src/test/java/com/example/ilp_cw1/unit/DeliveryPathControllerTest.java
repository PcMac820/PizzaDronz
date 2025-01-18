package com.example.ilp_cw1.unit;

import com.example.ilp_cw1.Controllers.DeliveryPathController;
import com.example.ilp_cw1.Controllers.OrderController;
import com.example.ilp_cw1.Controllers.PositioningController;
import com.example.ilp_cw1.Definitions.*;
import com.example.ilp_cw1.Services.CentralAreaService;
import com.example.ilp_cw1.Services.NoFlyZoneService;
import com.example.ilp_cw1.Services.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeliveryPathControllerTest {

    @Mock
    private OrderController orderController;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private CentralAreaService centralAreaService;

    @Mock
    private NoFlyZoneService noFlyZoneService;

    @Mock
    private PositioningController positioningController;

    @InjectMocks
    private DeliveryPathController deliveryPathController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initializes mocks
        deliveryPathController = new DeliveryPathController(orderController, restaurantService,
                centralAreaService, noFlyZoneService, positioningController);
    }

    @Test
    void testCalcDeliveryPath_withValidOrder_returnsPath() {
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{new Pizza("R1: Margarita", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = Collections.singletonList(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);
        when(orderController.validateOrder(order)).thenReturn(
                ResponseEntity.ok(new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR)));
        when(positioningController.getIsCloseTo(any())).thenReturn(ResponseEntity.ok(true));  // Simulate close to goal

        ResponseEntity<Object> response = deliveryPathController.calcDeliveryPath(order);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCalcDeliveryPath_withInvalidOrder_returnsBadRequest() {
        Order order = new Order("1", "2025-12-12", 100, null, new CreditCardInformation());  // Helper method to create an invalid order
        when(orderController.validateOrder(order)).thenReturn(ResponseEntity.ok(
                new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.TOTAL_INCORRECT)));

        ResponseEntity<Object> response = deliveryPathController.calcDeliveryPath(order);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid Order: Validate Order First", response.getBody());
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_withValidOrder_returnsGeoJsonPath() {
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{new Pizza("R1: Margarita", 1000)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        List<Restaurant> mockRestaurants = Collections.singletonList(
                new Restaurant("Restaurant1", new LngLat(-3.186, 55.944),
                        Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
                        List.of(new Pizza("R1: Margarita", 1000))));

        when(restaurantService.fetchRestaurants()).thenReturn(mockRestaurants);
        when(orderController.validateOrder(order)).thenReturn(ResponseEntity.ok(
                new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR)));
        when(positioningController.getIsCloseTo(any())).thenReturn(ResponseEntity.ok(true));

        ResponseEntity<String> response = deliveryPathController.calcDeliveryPathAsGeoJson(order);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof String);
    }

    @Test
    void testCalculateHeuristic_returnsCorrectValue() {
        LngLat from = new LngLat(0.0, 0.0);
        LngLat to = new LngLat(3.0, 4.0);
        double heuristic = deliveryPathController.calculateHeuristic(from, to);
        assertEquals(10, heuristic, 0.001);
    }

    @Test
    void testDoLinesIntersect_intersectingLines_returnsTrue() {
        NamedRegion noFlyZone = new NamedRegion("test1", Arrays.asList(
                new LngLat(-0.5, 0.5),
                new LngLat(0.5, 0.5),
                new LngLat(0.5, -0.5),
                new LngLat(-0.5, -0.5),
                new LngLat(-0.5, 0.5)));

        LngLat start = new LngLat(0.0, 0.0);
        LngLat end = new LngLat(1.0, 1.0);

        assertTrue(deliveryPathController.doLinesIntersect(noFlyZone, start, end));
    }

    @Test
    void testDoLinesIntersect_nonIntersectingLines_returnsFalse() {
        NamedRegion noFlyZone = new NamedRegion("test2", Arrays.asList(
                new LngLat(-0.5, 0.5),
                new LngLat(0.5, 0.5),
                new LngLat(0.5, -0.5),
                new LngLat(-0.5, -0.5),
                new LngLat(-0.5, 0.5)));

        LngLat start = new LngLat(-1.0, -1.0);
        LngLat end = new LngLat(-2.0, -2.0);

        assertFalse(deliveryPathController.doLinesIntersect(noFlyZone, start, end));
    }

}
