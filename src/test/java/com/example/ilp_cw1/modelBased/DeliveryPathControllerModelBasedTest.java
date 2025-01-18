package com.example.ilp_cw1.modelBased;

import com.example.ilp_cw1.Controllers.DeliveryPathController;
import com.example.ilp_cw1.Controllers.OrderController;
import com.example.ilp_cw1.Controllers.PositioningController;
import com.example.ilp_cw1.Definitions.*;
import com.example.ilp_cw1.Services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.test.web.servlet.*;

import java.time.DayOfWeek;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class DeliveryPathControllerModelBasedTest {

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

    private Order validOrder;
    private Order invalidOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validOrder = new Order();
        invalidOrder = new Order();
    }

    @Test
    public void testCalcDeliveryPath_InvalidOrder_ReturnsBadRequest() {
        invalidOrder.setPizzasInOrder(new Pizza[]{new Pizza("R1: Margarita", 1000)});
        when(orderController.validateOrder(invalidOrder)).thenReturn(new ResponseEntity<>(
                new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR), HttpStatus.OK));

        ResponseEntity<Object> response = deliveryPathController.calcDeliveryPath(invalidOrder);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testCalcDeliveryPath_PathNotFound_ReturnsBadRequest() {
        validOrder.setPizzasInOrder(new Pizza[]{new Pizza("R1: Margarita", 1000)});
        when(orderController.validateOrder(validOrder)).thenReturn(new ResponseEntity<>(
                new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR), HttpStatus.OK));
        when(positioningController.getIsCloseTo(any(LngLatPair.class))).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        ResponseEntity<Object> response = deliveryPathController.calcDeliveryPath(validOrder);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testCalcDeliveryPathAsGeoJson_PathNotFound_ReturnsBadRequest() {
        Order validOrder = new Order();
        when(positioningController.getIsCloseTo(any(LngLatPair.class))).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        ResponseEntity<String> response = deliveryPathController.calcDeliveryPathAsGeoJson(validOrder);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testSetupForAStarSearch_NoMatchingRestaurant_ThrowsException() {
        Order invalidOrder = new Order();
        invalidOrder.setPizzasInOrder(new Pizza[] { new Pizza("NonExistentPizza", 1000) });

        when(restaurantService.fetchRestaurants()).thenReturn(List.of(new Restaurant("Restaurant",
                new LngLat(-3.186874, 55.944494),
                List.of(String.valueOf(DayOfWeek.MONDAY)),
                List.of(new Pizza("R1: Margarita", 1000)))));

        assertThrows(IllegalArgumentException.class, () -> {
            deliveryPathController.setupForAStarSearch(invalidOrder);
        });
    }
}
