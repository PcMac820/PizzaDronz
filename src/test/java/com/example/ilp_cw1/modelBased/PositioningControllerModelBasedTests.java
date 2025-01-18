package com.example.ilp_cw1.modelBased;

import com.example.ilp_cw1.Definitions.LngLat;
import com.example.ilp_cw1.Definitions.NamedRegion;
import com.example.ilp_cw1.Definitions.NamedRegionAndPoint;
import com.example.ilp_cw1.Definitions.NextPosition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PositioningControllerModelBasedTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testValidCoordinatesAndValidAngle() {
        LngLat position = new LngLat(12.34, 56.78);
        NextPosition request = new NextPosition(position, 45.0);

        ResponseEntity<LngLat> response = restTemplate.postForEntity("/nextPosition", request, LngLat.class);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testInvalidCoordinates() {
        LngLat position = new LngLat(null, 200.0);
        NextPosition request = new NextPosition(position, 45.0);

        ResponseEntity<String> response = restTemplate.postForEntity("/nextPosition", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Start position and angle must be valid.", response.getBody());
    }

    @Test
    public void testInvalidAngle() {
        LngLat position = new LngLat(12.34, 56.78);
        NextPosition request = new NextPosition(position, -45.0);

        ResponseEntity<String> response = restTemplate.postForEntity("/nextPosition", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Start position and angle must be valid.", response.getBody());
    }

    @Test
    public void testPositionOutsideRegion() {
        LngLat point = new LngLat(10.0, 20.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0, 10.0),
                new LngLat(10.0, 10.0),
                new LngLat(10.0, 0.0),
                new LngLat(0.0, 0.0)
        );
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("region1", polygon));

        ResponseEntity<Boolean> response = restTemplate.postForEntity("/isInRegion", request, Boolean.class);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }

    @Test
    public void testPositionInsideRegion() {
        LngLat point = new LngLat(5.0, 5.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0, 10.0),
                new LngLat(10.0, 10.0),
                new LngLat(10.0, 0.0),
                new LngLat(0.0, 0.0)
        );
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("region1", polygon));

        ResponseEntity<Boolean> response = restTemplate.postForEntity("/isInRegion", request, Boolean.class);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    public void testNextPositionInvalidAngle() {
        LngLat position = new LngLat(12.34, 56.78);
        NextPosition request = new NextPosition(position, 37.5);

        ResponseEntity<String> response = restTemplate.postForEntity("/nextPosition", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Start position and angle must be valid.", response.getBody());
    }
}
