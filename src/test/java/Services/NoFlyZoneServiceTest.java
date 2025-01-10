package Services;

import com.example.ilp_cw1.DTO.LngLat;
import com.example.ilp_cw1.DTO.NamedRegion;
import com.example.ilp_cw1.Services.CentralAreaService;
import com.example.ilp_cw1.Services.NoFlyZoneService;
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

public class NoFlyZoneServiceTest {
    @Mock
    private HttpClient mockHttpClient;

    @InjectMocks
    private NoFlyZoneService noFlyZoneService;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFetchNoFlyZones_SuccessResponse() throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        String jsonResponse = "["
                + "{ \"name\": \"George Square Area\", \"vertices\": [ { \"lng\": -3.190578818321228, \"lat\": 55.94402412577528 }, { \"lng\": -3.1899887323379517, \"lat\": 55.94284650540911 }, { \"lng\": -3.187097311019897, \"lat\": 55.94328811724263 }, { \"lng\": -3.187682032585144, \"lat\": 55.944477740393744 }, { \"lng\": -3.190578818321228, \"lat\": 55.94402412577528 } ] },"
                + "{ \"name\": \"Dr Elsie Inglis Quadrangle\", \"vertices\": [ { \"lng\": -3.1907182931900024, \"lat\": 55.94519570234043 }, { \"lng\": -3.1906163692474365, \"lat\": 55.94498241796357 }, { \"lng\": -3.1900262832641597, \"lat\": 55.94507554227258 }, { \"lng\": -3.190133571624756, \"lat\": 55.94529783810495 }, { \"lng\": -3.1907182931900024, \"lat\": 55.94519570234043 } ] },"
                + "{ \"name\": \"Bristo Square Open Area\", \"vertices\": [ { \"lng\": -3.189543485641479, \"lat\": 55.94552313663306 }, { \"lng\": -3.189382553100586, \"lat\": 55.94553214854692 }, { \"lng\": -3.189259171485901, \"lat\": 55.94544803726933 }, { \"lng\": -3.1892001628875732, \"lat\": 55.94533688994374 }, { \"lng\": -3.189194798469543, \"lat\": 55.94519570234043 }, { \"lng\": -3.189135789871216, \"lat\": 55.94511759833873 }, { \"lng\": -3.188138008117676, \"lat\": 55.9452738061846 }, { \"lng\": -3.1885510683059692, \"lat\": 55.946105902745614 }, { \"lng\": -3.1895381212234497, \"lat\": 55.94555918427592 }, { \"lng\": -3.189543485641479, \"lat\": 55.94552313663306 } ] },"
                + "{ \"name\": \"Bayes Central Area\", \"vertices\": [ { \"lng\": -3.1876927614212036, \"lat\": 55.94520696732767 }, { \"lng\": -3.187555968761444, \"lat\": 55.9449621408666 }, { \"lng\": -3.186981976032257, \"lat\": 55.94505676722831 }, { \"lng\": -3.1872327625751495, \"lat\": 55.94536993377657 }, { \"lng\": -3.1874459981918335, \"lat\": 55.9453361389472 }, { \"lng\": -3.1873735785484314, \"lat\": 55.94519344934259 }, { \"lng\": -3.1875935196876526, \"lat\": 55.94515665035927 }, { \"lng\": -3.187624365091324, \"lat\": 55.94521973430925 }, { \"lng\": -3.1876927614212036, \"lat\": 55.94520696732767 } ] }"
                + "]";
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        List<NamedRegion> result = noFlyZoneService.fetchNoFlyZones();

        assertNotNull(result);

        List<NamedRegion> expected = List.of(
                new NamedRegion("George Square Area", List.of(
                        new LngLat(-3.190578818321228, 55.94402412577528),
                        new LngLat(-3.1899887323379517, 55.94284650540911),
                        new LngLat(-3.187097311019897, 55.94328811724263),
                        new LngLat(-3.187682032585144, 55.944477740393744),
                        new LngLat(-3.190578818321228, 55.94402412577528)
                )),
                new NamedRegion("Dr Elsie Inglis Quadrangle", List.of(
                        new LngLat(-3.1907182931900024, 55.94519570234043),
                        new LngLat(-3.1906163692474365, 55.94498241796357),
                        new LngLat(-3.1900262832641597, 55.94507554227258),
                        new LngLat(-3.190133571624756, 55.94529783810495),
                        new LngLat(-3.1907182931900024, 55.94519570234043)
                )),
                new NamedRegion("Bristo Square Open Area", List.of(
                        new LngLat(-3.189543485641479, 55.94552313663306),
                        new LngLat(-3.189382553100586, 55.94553214854692),
                        new LngLat(-3.189259171485901, 55.94544803726933),
                        new LngLat(-3.1892001628875732, 55.94533688994374),
                        new LngLat(-3.189194798469543, 55.94519570234043),
                        new LngLat(-3.189135789871216, 55.94511759833873),
                        new LngLat(-3.188138008117676, 55.9452738061846),
                        new LngLat(-3.1885510683059692, 55.946105902745614),
                        new LngLat(-3.1895381212234497, 55.94555918427592),
                        new LngLat(-3.189543485641479, 55.94552313663306)
                )),
                new NamedRegion("Bayes Central Area", List.of(
                        new LngLat(-3.1876927614212036, 55.94520696732767),
                        new LngLat(-3.187555968761444, 55.9449621408666),
                        new LngLat(-3.186981976032257, 55.94505676722831),
                        new LngLat(-3.1872327625751495, 55.94536993377657),
                        new LngLat(-3.1874459981918335, 55.9453361389472),
                        new LngLat(-3.1873735785484314, 55.94519344934259),
                        new LngLat(-3.1875935196876526, 55.94515665035927),
                        new LngLat(-3.187624365091324, 55.94521973430925),
                        new LngLat(-3.1876927614212036, 55.94520696732767)
                ))
        );
        assertEquals(expected, result);
    }

    @Test
    void fetchNoFlyZones_returnsNull_whenResponseIsNotSuccessful() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        List<NamedRegion> result = noFlyZoneService.fetchNoFlyZones();

        assertNull(result);
    }

    @Test
    void fetchNoFlyZones_handlesExceptionAndReturnsNull() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new RuntimeException("Error occurred"));

        List<NamedRegion> result = noFlyZoneService.fetchNoFlyZones();

        assertNull(result);
    }
}
