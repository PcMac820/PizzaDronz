package com.example.ilp_cw1.Services;

import com.example.ilp_cw1.DTO.NamedRegion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CentralAreaService {

    private static final String CENTRAL_AREA_URL = "https://ilp-rest-2024.azurewebsites.net/centralArea";

    public NamedRegion fetchCentralArea() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(CENTRAL_AREA_URL)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), new TypeReference<>() {
                });
            }
            else {
                System.err.println("Error: Unable to fetch data. Status code: " + response.statusCode());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}