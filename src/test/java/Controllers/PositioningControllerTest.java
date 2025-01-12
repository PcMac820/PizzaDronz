package Controllers;

import com.example.ilp_cw1.Controllers.*;
import com.example.ilp_cw1.DTO.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PositioningControllerTest {

    @InjectMocks
    private PositioningController positioningController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        positioningController = new PositioningController();
    }

    // TEst
    @Test
    public void testGetDistanceTo_ValidCoordinates() {
        LngLat position1 = new LngLat(0.0, 0.0);
        LngLat position2 = new LngLat(0.0, 1.0);
        LngLatPair pair = new LngLatPair(position1, position2);

        ResponseEntity<Object> response = positioningController.getDistanceTo(pair);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1.0, (double) response.getBody(), 0.01);  // Check if the distance is approximately 1
    }

    @Test
    public void testGetDistanceTo_InvalidCoordinates() {
        LngLat position1 = new LngLat(200.0, 0.0); // Invalid longitude
        LngLat position2 = new LngLat(0.0, 0.0);
        LngLatPair pair = new LngLatPair(position1, position2);

        ResponseEntity<Object> response = positioningController.getDistanceTo(pair);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid coordinates"));
    }

    @Test
    public void testGetIsCloseTo_ValidCoordinates_Close() {
        LngLat position1 = new LngLat(0.0, 0.0);
        LngLat position2 = new LngLat(0.0, 0.0001); // Close positions
        LngLatPair pair = new LngLatPair(position1, position2);

        ResponseEntity<Object> response = positioningController.getIsCloseTo(pair);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    public void testGetIsCloseTo_ValidCoordinates_NotClose() {
        LngLat position1 = new LngLat(0.0, 0.0);
        LngLat position2 = new LngLat(0.0, 0.001); // Not close
        LngLatPair pair = new LngLatPair(position1, position2);

        ResponseEntity<Object> response = positioningController.getIsCloseTo(pair);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody());
    }

    @Test
    public void testGetIsCloseTo_InvalidCoordinates() {
        LngLat position1 = new LngLat(200.0, 0.0); // Invalid longitude
        LngLat position2 = new LngLat(0.0, 0.0);
        LngLatPair pair = new LngLatPair(position1, position2);

        ResponseEntity<Object> response = positioningController.getIsCloseTo(pair);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid Positions"));
    }

    @Test
    public void testGetNextPosition_ValidCoordinates() {
        LngLat start = new LngLat(0.0, 0.0);
        double angle = 90;
        NextPosition request = new NextPosition(start, angle);

        ResponseEntity<Object> response = positioningController.getNextPosition(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LngLat result = (LngLat) response.getBody();
        assertNotNull(result);
        assertEquals(0.00015, result.getLat(), 0.00001);
        assertEquals(0.0, result.getLng(), 0.00001);
    }

    @Test
    public void testGetNextPosition_InvalidCoordinates() {
        LngLat start = new LngLat(200.0, 0.0); // Invalid longitude
        double angle = 90;
        NextPosition request = new NextPosition(start, angle);

        ResponseEntity<Object> response = positioningController.getNextPosition(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid coordinates"));
    }

    @Test
    public void testIsInRegion_ValidPointInsideRegion() {
        LngLat point = new LngLat(0.0, 0.0);
        List<LngLat> polygon = Arrays.asList(
                new LngLat(-1.0, -1.0), new LngLat(1.0, -1.0),
                new LngLat(1.0, 1.0), new LngLat(-1.0, 1.0), new LngLat(-1.0, -1.0)
        );
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("test1", polygon));

        ResponseEntity<Boolean> response = positioningController.isInRegion(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    public void testIsInRegion_ValidPointOutsideRegion() {
        LngLat point = new LngLat(2.0, 2.0); // Outside the region
        List<LngLat> polygon = Arrays.asList(
                new LngLat(-1.0, -1.0), new LngLat(1.0, -1.0),
                new LngLat(1.0, 1.0), new LngLat(-1.0, 1.0), new LngLat(-1.0, -1.0)
        );
        NamedRegionAndPoint request = new NamedRegionAndPoint(point, new NamedRegion("test2", polygon));

        ResponseEntity<Boolean> response = positioningController.isInRegion(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }
}
