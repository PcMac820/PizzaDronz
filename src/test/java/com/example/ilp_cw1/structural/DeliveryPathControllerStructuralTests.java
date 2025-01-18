package com.example.ilp_cw1.structural;

import com.example.ilp_cw1.Controllers.DeliveryPathController;
import com.example.ilp_cw1.Controllers.OrderController;
import com.example.ilp_cw1.Controllers.PositioningController;
import com.example.ilp_cw1.Definitions.*;
import com.example.ilp_cw1.Services.CentralAreaService;
import com.example.ilp_cw1.Services.NoFlyZoneService;
import com.example.ilp_cw1.Services.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DeliveryPathControllerStructuralTests {

    @Autowired
    private DeliveryPathController deliveryPathController;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private CentralAreaService centralAreaService;

    @MockBean
    private NoFlyZoneService noFlyZoneService;

    @MockBean
    private PositioningController positioningController;

    @MockBean
    private OrderController orderController;

    @Mock
    private Restaurant restaurant;

    @Mock
    private Pizza pizza;

    @Mock
    private LngLat restaurantLocation;

    @Mock
    private LngLat goalLocation;

    @BeforeEach
    public void setUp() {
        pizza = new Pizza("R1: Margarita", 1000);
        restaurantLocation = new LngLat(-3.186874, 55.944494);  // Sample coordinates
        goalLocation = new LngLat(-3.188874, 55.946494);  // Sample goal coordinates

        restaurant = mock(Restaurant.class);
        when(restaurant.getLocation()).thenReturn(restaurantLocation);
        when(restaurant.getMenu()).thenReturn(Collections.singletonList(pizza));

        // Mocking the restaurantService to return a list with the restaurant that contains the pizza
        when(restaurantService.fetchRestaurants()).thenReturn(Collections.singletonList(restaurant));

        // Mocking central area and no-fly zones
        NamedRegion centralArea = new NamedRegion();  // Sample central area
        when(centralAreaService.fetchCentralArea()).thenReturn(centralArea);

        NamedRegion noFlyZone = new NamedRegion();  // Sample no-fly zone
        when(noFlyZoneService.fetchNoFlyZones()).thenReturn(Collections.singletonList(noFlyZone));

        // Mocking order validation to prevent "help" message
        when(orderController.validateOrder(any(Order.class))).thenReturn(new ResponseEntity<>(
                new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR), HttpStatus.OK));
    }

    @Test
    public void testSetupForAStarSearch_FindsRestaurant_ReturnsCorrectStartAndGoal() {
        LngLat startPosition = new LngLat(-3.186874, 55.944494);
        LngLat goalPosition = new LngLat(-3.188874, 55.946494);

        when(positioningController.getIsCloseTo(any(LngLatPair.class)))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        // Call the method
        List<LngLat> path = deliveryPathController.findPathUsingAStar(startPosition, goalPosition);

        // Perform assertions
        assertNotNull(path);
        assertEquals(1, path.size());
        assertEquals(startPosition, path.get(0));
    }

    @Test
    public void testSetupForAStarSearch_NoMatchingRestaurant_ThrowsException() {
        // Create an order with a pizza not in the available restaurant menu
        Pizza invalidPizza = new Pizza("InvalidPizza", 1000);
        Order invalidOrder = new Order("12345", "2025-12-12", 1100, new Pizza[]{invalidPizza}, null);

        // Mocking restaurantService to return an empty list
        when(restaurantService.fetchRestaurants()).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> {
            deliveryPathController.setupForAStarSearch(invalidOrder);
        });
    }

    @Test
    public void testFindPathUsingAStar_ValidStartAndGoal_ReturnsValidPath() {
        LngLat startPosition = new LngLat(-3.186874, 55.944494);
        LngLat goalPosition = new LngLat(-3.188874, 55.946494);

        List<LngLat> validPath = Arrays.asList(startPosition, goalPosition);
        when(positioningController.getIsCloseTo(any(LngLatPair.class))).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        List<LngLat> path = deliveryPathController.findPathUsingAStar(startPosition, goalPosition);

        assertNotNull(path);
        assertEquals(1, path.size());
        assertEquals(startPosition, path.get(0));
    }

    @Test
    public void testFormatPathToGeoJson_ValidPath_ReturnsGeoJson() {
        // Create a valid path
        List<LngLat> path = Arrays.asList(new LngLat(-3.186874, 55.944494), new LngLat(-3.188874, 55.946494));

        // Call the method to convert the path into GeoJSON format
        String geoJson = deliveryPathController.formatPathToGeoJson(path);

        assertNotNull(geoJson);
        assertTrue(geoJson.contains("LineString")); // Check if GeoJSON contains the correct type
    }

    @Test
    public void testFormatPathToGeoJson_EmptyPath_ReturnsEmptyGeoJson() {
        List<LngLat> emptyPath = Collections.emptyList();

        String geoJson = deliveryPathController.formatPathToGeoJson(emptyPath);

        assertNotNull(geoJson);
        assertTrue(geoJson.contains("LineString"));
        assertTrue(geoJson.contains("\"coordinates\" : [ ]"));
    }
}