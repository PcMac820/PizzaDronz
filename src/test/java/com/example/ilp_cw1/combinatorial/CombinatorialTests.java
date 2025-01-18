package com.example.ilp_cw1.combinatorial;

import com.example.ilp_cw1.Controllers.PositioningController;
import com.example.ilp_cw1.Definitions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CombinatorialTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PositioningController positioningController;

    @Test
    public void testGetDistanceTo_CombinatorialCoordinates() {
        List<LngLat> positions1 = Arrays.asList(
                new LngLat(12.34, 56.78),
                new LngLat(23.45, 67.89),
                new LngLat(34.56, 78.90)
        );

        List<LngLat> positions2 = Arrays.asList(
                new LngLat(12.34, 67.89),
                new LngLat(23.45, 78.90),
                new LngLat(34.56, 89.01)
        );

        for (LngLat pos1 : positions1) {
            for (LngLat pos2 : positions2) {
                LngLatPair request = new LngLatPair(pos1, pos2);

                ResponseEntity<Double> response = restTemplate.postForEntity("/distanceTo", request, Double.class);

                assertNotNull(response);
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertNotNull(response.getBody());

                double expectedDistance = positioningController.calculateDistance(pos1, pos2);
                assertEquals(expectedDistance, response.getBody(), 0.01);
            }
        }
    }
    @Test
    public void testGetIsCloseTo_Combinatorial() {
        List<LngLat> positions1 = Arrays.asList(
                new LngLat(12.34, 56.78),
                new LngLat(13.34, 57.78),
                new LngLat(14.34, 58.78)
        );

        List<LngLat> positions2 = Arrays.asList(
                new LngLat(12.34, 56.78015),
                new LngLat(15.34, 59.78),
                new LngLat(16.34, 60.78)
        );

        for (LngLat pos1 : positions1) {
            for (LngLat pos2 : positions2) {
                LngLatPair request = new LngLatPair(pos1, pos2);

                ResponseEntity<Boolean> response = restTemplate.postForEntity("/isCloseTo", request, Boolean.class);

                assertNotNull(response);
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertNotNull(response.getBody());

                boolean expectedResult = positioningController.calculateDistance(pos1, pos2) < 0.00015;
                assertEquals(expectedResult, response.getBody());
            }
        }
    }

    @Test
    public void testGetNextPosition_Combinatorial() {
        List<LngLat> startPositions = Arrays.asList(
                new LngLat(12.34, 56.78),
                new LngLat(23.45, 67.89),
                new LngLat(34.56, 78.90)
        );

        List<Double> angles = Arrays.asList(0.0, 45.0, 90.0, 180.0, 270.0, 360.0);

        for (LngLat start : startPositions) {
            for (double angle : angles) {
                NextPosition request = new NextPosition(start, angle);

                ResponseEntity<LngLat> response = restTemplate.postForEntity("/nextPosition", request, LngLat.class);

                assertNotNull(response);
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertNotNull(response.getBody());

                LngLat expectedPosition = positioningController.calculateNextPosition(
                        start.getLng(), start.getLat(), 0.00015, angle);
                assertEquals(expectedPosition, response.getBody());
            }
        }
    }

    @Test
    public void testIsInRegion_Combinatorial() {
        List<LngLat> points = Arrays.asList(
                new LngLat(1.0, 2.0),
                new LngLat(2.0, 3.0),
                new LngLat(3.0, 4.0)
        );

        List<NamedRegion> regions = Arrays.asList(
                new NamedRegion("Region1", Arrays.asList(
                        new LngLat(0.0, 0.0),
                        new LngLat(0.0, 3.0),
                        new LngLat(3.0, 3.0),
                        new LngLat(3.0, 0.0),
                        new LngLat(0.0, 0.0)
                )),
                new NamedRegion("Region2", Arrays.asList(
                        new LngLat(2.0, 2.0),
                        new LngLat(2.0, 5.0),
                        new LngLat(5.0, 5.0),
                        new LngLat(5.0, 2.0),
                        new LngLat(2.0, 2.0)
                ))
        );

        for (LngLat point : points) {
            for (NamedRegion region : regions) {
                NamedRegionAndPoint request = new NamedRegionAndPoint(point, region);

                ResponseEntity<Boolean> response = restTemplate.postForEntity("/isInRegion", request, Boolean.class);

                assertNotNull(response);
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertNotNull(response.getBody());

                boolean expectedResult = positioningController.isPointInPolygon(point, region.getVertices());
                assertEquals(expectedResult, response.getBody());
            }
        }
    }
}
