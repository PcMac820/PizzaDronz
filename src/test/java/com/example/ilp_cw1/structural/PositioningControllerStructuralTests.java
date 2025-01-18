package com.example.ilp_cw1.structural;

import com.example.ilp_cw1.Controllers.PositioningController;
import com.example.ilp_cw1.Definitions.LngLat;
import com.example.ilp_cw1.Definitions.LngLatPair;
import com.example.ilp_cw1.Definitions.NamedRegion;
import com.example.ilp_cw1.Definitions.NamedRegionAndPoint;
import com.example.ilp_cw1.Definitions.NextPosition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PositioningControllerStructuralTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PositioningController positioningController;

    // Test if the PositioningController bean is created successfully
    @Test
    public void testControllerBeanInjection() {
        assertNotNull(positioningController, "PositioningController bean should be injected successfully.");
    }

    @Test
    public void testIsAlive() {
        boolean result = restTemplate.getForObject("/isAlive", Boolean.class);
        assertTrue(result);
    }

    @Test
    public void testUuid() {
        ResponseEntity<String> response = restTemplate.getForEntity("/uuid", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("s2330567", response.getBody());
    }

    @Test
    public void testGetDistanceTo_ValidCoordinates() {
        LngLat position1 = new LngLat(12.34, 56.78);
        LngLat position2 = new LngLat(13.34, 57.78);
        LngLatPair request = new LngLatPair(position1, position2);

        String baseUrl = "http://localhost:" + port;
        String url = baseUrl + "/distanceTo";

        ResponseEntity<Double> response = restTemplate.postForEntity(url, request, Double.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetDistanceTo_InvalidCoordinates() {
        LngLat position1 = new LngLat(-200.0, 95.0);
        LngLat position2 = new LngLat(12.34, 56.78);
        LngLatPair request = new LngLatPair(position1, position2);

        ResponseEntity<String> response = restTemplate.postForEntity("/distanceTo", request, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid coordinates.", response.getBody());
    }

    @Test
    public void testGetIsCloseTo_WithinRange() {
        LngLat position1 = new LngLat(12.34, 56.78);
        LngLat position2 = new LngLat(12.3401, 56.7801);
        LngLatPair request = new LngLatPair(position1, position2);

        ResponseEntity<Boolean> response = restTemplate.postForEntity("/isCloseTo", request, Boolean.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    public void testGetIsCloseTo_OutOfRange() {
        LngLat position1 = new LngLat(12.34, 56.78);
        LngLat position2 = new LngLat(13.34, 57.78);
        LngLatPair request = new LngLatPair(position1, position2);

        ResponseEntity<Boolean> response = restTemplate.postForEntity("/isCloseTo", request, Boolean.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }

    @Test
    public void testGetNextPosition_InvalidAngle() {
        NextPosition request = new NextPosition(new LngLat(12.34, 56.78), -10.0);
        ResponseEntity<String> response = restTemplate.postForEntity("/nextPosition", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Start position and angle must be valid.", response.getBody());
    }

    @Test
    public void testGetNextPosition_ValidPositionAndAngle() {
        NextPosition request = new NextPosition(new LngLat(12.34, 56.78), 0.0);
        ResponseEntity<LngLat> response = restTemplate.postForEntity("/nextPosition", request, LngLat.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LngLat result = response.getBody();
        assertNotNull(result);
        assertTrue(result.getLng() > 12.34);
    }

    @Test
    public void testIsInRegion_ValidPolygon() {
        LngLat point = new LngLat(1.0, 1.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(2.0, 0.0),
                new LngLat(2.0, 2.0),
                new LngLat(0.0, 0.0)
        );
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("ValidRegion", polygon));

        ResponseEntity<Boolean> response = restTemplate.postForEntity("/isInRegion", request, Boolean.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    public void testIsInRegion_InvalidPolygon() {
        LngLat point = new LngLat(1.0, 1.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(2.0, 2.0),
                new LngLat(4.0, 4.0)  // Collinear points
        );
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("InvalidRegion", polygon));

        ResponseEntity<Boolean> response = restTemplate.postForEntity("/isInRegion", request, Boolean.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody());
    }

    @Test
    public void testIsInRegion_PointOnBoundary() {
        LngLat point = new LngLat(1.0, 1.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(2.0, 0.0),
                new LngLat(2.0, 2.0),
                new LngLat(0.0, 0.0)
        );
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("BoundaryRegion", polygon));

        ResponseEntity<Boolean> response = restTemplate.postForEntity("/isInRegion", request, Boolean.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }
}

