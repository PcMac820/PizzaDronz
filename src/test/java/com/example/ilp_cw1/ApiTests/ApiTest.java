package com.example.ilp_cw1.ApiTests;

import com.example.ilp_cw1.Definitions.*;
import com.example.ilp_cw1.Services.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestaurantService restaurantService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldReturnIsAlive() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(get("/isAlive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void shouldReturnUuidString() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(get("/uuid")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("s2330567"));
    }

    @Test
    public void testGetDistanceTo_Success() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLatPair request = new LngLatPair(new LngLat(0.0, 0.0), new LngLat(0.0, 1.0));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("1.0"));
    }

    @Test
    public void testGetDistanceTo_InvalidCoordinates() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLatPair request = new LngLatPair(new LngLat(1000.0, 1000.0), new LngLat(-1000.0, -1000.0));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid coordinates."));
    }

    @Test
    public void testGetDistanceTo_MalformedJson() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        String malformedJson = "{ \"position1\": { \"lng\": 0.0, \"lat\": 0.0 }";

        mockMvc.perform(post("/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_ClosePositions() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLatPair request = new LngLatPair(new LngLat(0.0, 0.0), new LngLat(0.0, 0.0001));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsCloseTo_FarPositions() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLatPair request = new LngLatPair(new LngLat(0.0, 0.0), new LngLat(1.0, 1.0));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));  // Expect "false"
    }

    @Test
    public void testIsCloseTo_UnexpectedError() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        String malformedJson = "{ \"position1\": { \"lng\": 0.0, \"lat\": 0.0 }";  // Malformed JSON input

        mockMvc.perform(post("/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetNextPosition_ValidInput() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        NextPosition request = new NextPosition(new LngLat(0.0, 0.0), 90.0);
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    BigDecimal lng = new ObjectMapper().readTree(result.getResponse().getContentAsString())
                            .path("lng").decimalValue();
                    BigDecimal lat = new ObjectMapper().readTree(result.getResponse().getContentAsString())
                            .path("lat").decimalValue();

                    assertThat(lng.doubleValue(), closeTo(0.0, 1e-10));
                    assertThat(lat.doubleValue(), closeTo(0.00015, 1e-10));
                });
    }

    @Test
    public void testGetNextPosition_InvalidAngle() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        NextPosition request = new NextPosition(new LngLat(0.0, 0.0), 23.0);
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start position and angle must be valid."));
    }

    @Test
    public void testGetNextPosition_InvalidCoordinates() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        NextPosition request = new NextPosition(new LngLat(200.0, 200.0), 45.0);
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid coordinates."));
    }

    @Test
    public void testGetNextPosition_OutOfBounds() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        NextPosition request = new NextPosition(new LngLat(179.9999, 89.9999), 0.0);
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Travelled out of this world."));
    }

    @Test
    public void testGetNextPosition_UnexpectedError() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        String malformedJson = "{ \"start\": { \"lng\": 0.0, \"lat\": 0.0 }";

        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_ValidPointInside() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLat point = new LngLat(1.0, 1.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0, 2.0),
                new LngLat(2.0, 2.0),
                new LngLat(2.0, 0.0),
                new LngLat(0.0, 0.0)
        );

        NamedRegion region = new NamedRegion("TestRegion", polygon);
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, region);

        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsInRegion_ValidPointOutside() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLat point = new LngLat(3.0, 3.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0, 2.0),
                new LngLat(2.0, 2.0),
                new LngLat(2.0, 0.0),
                new LngLat(0.0, 0.0)
        );

        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("TestRegion", polygon));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_InvalidPoint() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLat point = new LngLat(null, null);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0, 2.0),
                new LngLat(2.0, 2.0),
                new LngLat(2.0, 0.0),
                new LngLat(0.0, 0.0)
        );

        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("TestRegion", polygon));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_InvalidPolygon() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLat point = new LngLat(1.0, 1.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0, 2.0)
        );

        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("TestRegion", polygon));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_InvalidCollinearPolygon() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLat point = new LngLat(1.0, 1.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(1.0, 1.0),
                new LngLat(2.0, 2.0)
        );

        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("TestRegion", polygon));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_EmptyRegion() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        LngLat point = new LngLat(1.0, 1.0);
        List<LngLat> polygon = new ArrayList<>();

        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("TestRegion", polygon));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));
    }

    @Test
    public void testValidateOrder_EmptyOrder_ShouldReturnInvalidWithEmptyOrderCode() throws Exception {
        Order order = new Order("1", "2025-12-12", 200,
                new Pizza[]{new Pizza("name", 100)},
                new CreditCardInformation("1234567812345678", "12/25", "333"));
        order.setPizzasInOrder(new Pizza[]{});

        mockMvc.perform(post("/validateOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.INVALID.toString()))
                .andExpect(jsonPath("$.orderValidationCode").value(OrderValidationCode.EMPTY_ORDER.toString()));
    }

    @Test
    public void testValidateOrder_InvalidPizzaDefinition_ShouldReturnInvalidWithPizzaNotDefinedCode() throws Exception {
        Pizza pizza = new Pizza();
        pizza.setName("null");
        pizza.setPriceInPence(0);
        Order order = new Order("1", "2025-12-12", 200,
                new Pizza[]{new Pizza("R1: Margarita", 1000), pizza},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        mockMvc.perform(post("/validateOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.INVALID.toString()))
                .andExpect(jsonPath("$.orderValidationCode").value(OrderValidationCode.PIZZA_NOT_DEFINED.toString()));
    }

    @Test
    public void testValidateOrder_MultipleRestaurants_ShouldReturnInvalidWithMultipleRestaurantsCode() throws Exception {
        Pizza pizza1 = new Pizza("R1: Margarita", 1000);
        Pizza pizza2 = new Pizza("R2: Meat Lover", 1400);
        Order order = new Order("1", "2025-12-12", 2500,
                new Pizza[]{pizza1, pizza2},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        mockMvc.perform(post("/validateOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.INVALID.toString()))
                .andExpect(jsonPath("$.orderValidationCode").value(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS.toString()));
    }

    @Test
    public void testValidateOrder_MaxPizzaCountExceeded_ShouldReturnInvalidWithMaxPizzaCountExceededCode() throws Exception {
        Pizza pizza = new Pizza("R1: Margarita", 1000);
        Order order = new Order("1", "2025-12-12", 5100,
                new Pizza[]{pizza, pizza, pizza, pizza, pizza},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        mockMvc.perform(post("/validateOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.INVALID.toString()))
                .andExpect(jsonPath("$.orderValidationCode").value(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED.toString()));
    }

    @Test
    public void testValidateOrder_InvalidTotalPrice_ShouldReturnInvalidWithTotalIncorrectCode() throws Exception {
        Pizza pizza = new Pizza("R1: Margarita", 1000);
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{pizza},
                new CreditCardInformation("1234567812345678", "12/25", "333"));
        order.setPriceTotalInPence(500);

        mockMvc.perform(post("/validateOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.INVALID.toString()))
                .andExpect(jsonPath("$.orderValidationCode").value(OrderValidationCode.TOTAL_INCORRECT.toString()));
    }

    @Test
    public void testValidateOrder_InvalidCVV_ShouldReturnInvalidWithCVVInvalidCode() throws Exception {
        Pizza pizza = new Pizza("R1: Margarita", 1000);
        CreditCardInformation cardInfo = new CreditCardInformation("1234567812345678", "12/25", "12");  // Invalid CVV
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{pizza},
                new CreditCardInformation("1234567812345678", "12/25", "333"));
        order.setCreditCardInformation(cardInfo);

        mockMvc.perform(post("/validateOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.INVALID.toString()))
                .andExpect(jsonPath("$.orderValidationCode").value(OrderValidationCode.CVV_INVALID.toString()));
    }

    @Test
    public void testCalcDeliveryPath_InvalidOrder_ShouldReturnBadRequest() throws Exception {
        Order order = new Order("1", "2025-12-12", 100,
                new Pizza[]{},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        mockMvc.perform(post("/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Order: Validate Order First"));
    }

    @Test
    public void testCalcDeliveryPathAsGeoJson_ValidOrder_ShouldReturnGeoJson() throws Exception {
        Pizza pizza = new Pizza("R1: Margarita", 1000);
        Order order = new Order("1", "2025-12-12", 1100,
                new Pizza[]{pizza},
                new CreditCardInformation("1234567812345678", "12/25", "333"));

        mockMvc.perform(post("/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCalcDeliveryPath_ExceptionHandling_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Invalid Input\""))
                .andExpect(status().isBadRequest());
    }
}
