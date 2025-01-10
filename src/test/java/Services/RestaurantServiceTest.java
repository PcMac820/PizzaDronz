package Services;

import com.example.ilp_cw1.DTO.LngLat;
import com.example.ilp_cw1.DTO.NamedRegion;
import com.example.ilp_cw1.DTO.Pizza;
import com.example.ilp_cw1.DTO.Restaurant;
import com.example.ilp_cw1.Services.CentralAreaService;
import com.example.ilp_cw1.Services.RestaurantService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantServiceTest {
    @Mock
    private HttpClient mockHttpClient;

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFetchRestaurants_SuccessResponse() throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);

        String jsonResponse = "[" +
                "{" +
                "\"name\": \"Civerinos Slice\"," +
                "\"location\": { \"lng\": -3.1912869215011597, \"lat\": 55.945535152517735 }," +
                "\"openingDays\": [\"MONDAY\", \"TUESDAY\", \"FRIDAY\", \"SATURDAY\", \"SUNDAY\"]," +
                "\"menu\": [{" +
                "\"name\": \"R1: Margarita\", \"priceInPence\": 1000" +
                "}, {" +
                "\"name\": \"R1: Calzone\", \"priceInPence\": 1400" +
                "}]" +
                "}," +
                "{" +
                "\"name\": \"Sora Lella Vegan Restaurant\"," +
                "\"location\": { \"lng\": -3.202541470527649, \"lat\": 55.943284737579376 }," +
                "\"openingDays\": [\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\", \"THURSDAY\", \"FRIDAY\"]," +
                "\"menu\": [{" +
                "\"name\": \"R2: Meat Lover\", \"priceInPence\": 1400" +
                "}, {" +
                "\"name\": \"R2: Vegan Delight\", \"priceInPence\": 1100" +
                "}]" +
                "}," +
                "{" +
                "\"name\": \"Domino's Pizza - Edinburgh - Southside\"," +
                "\"location\": { \"lng\": -3.1838572025299072, \"lat\": 55.94449876875712 }," +
                "\"openingDays\": [\"WEDNESDAY\", \"THURSDAY\", \"FRIDAY\", \"SATURDAY\", \"SUNDAY\"]," +
                "\"menu\": [{" +
                "\"name\": \"R3: Super Cheese\", \"priceInPence\": 1400" +
                "}, {" +
                "\"name\": \"R3: All Shrooms\", \"priceInPence\": 900" +
                "}]" +
                "}" +
                "]";
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        List<Restaurant> result = restaurantService.fetchRestaurants();

        assertNotNull(result);

        Restaurant expectedRestaurant1 = new Restaurant(
                "Civerinos Slice",
                new LngLat(-3.1912869215011597, 55.945535152517735),
                List.of("MONDAY", "TUESDAY", "FRIDAY", "SATURDAY", "SUNDAY"),
                List.of(new Pizza("R1: Margarita", 1000), new Pizza("R1: Calzone", 1400))
        );

        assertEquals(3, result.size());
        assertEquals(expectedRestaurant1, result.get(0));
    }


    @Test
    void fetchRestaurants_returnsNull_whenResponseIsNotSuccessful() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        List<Restaurant> result = restaurantService.fetchRestaurants();

        assertNull(result);
    }

    @Test
    void fetchRestaurants_handlesExceptionAndReturnsNull() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new RuntimeException("Error occurred"));

        List<Restaurant> result = restaurantService.fetchRestaurants();

        assertNull(result);
    }
}
