package Services;

import com.example.ilp_cw1.DTO.NamedRegion;
import com.example.ilp_cw1.Services.CentralAreaService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CentralAreaServiceTest {
    @Mock
    private HttpClient mockHttpClient;

    @InjectMocks
    private CentralAreaService centralAreaService;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFetchCentralArea_SuccessResponse() throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"name\": \"central\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        NamedRegion result = centralAreaService.fetchCentralArea();

        assertNotNull(result);
        assertEquals("central", result.getName().toLowerCase()); // Case-insensitive check
    }

    @Test
    void fetchCentralArea_returnsNull_whenResponseIsNotSuccessful() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        NamedRegion result = centralAreaService.fetchCentralArea();

        assertNull(result);
    }

    @Test
    void fetchCentralArea_handlesExceptionAndReturnsNull() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new RuntimeException("Error occurred"));

        NamedRegion result = centralAreaService.fetchCentralArea();

        assertNull(result);
    }
}
