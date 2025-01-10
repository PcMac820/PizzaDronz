package com.example.ilp_cw1.Services;

import com.example.ilp_cw1.DTO.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class NoFlyZoneService {

    private final HttpClient httpClient;
    private static final String NO_FLY_ZONE_URL = "https://ilp-rest-2024.azurewebsites.net/noFlyZones";

    public NoFlyZoneService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<NamedRegion> fetchNoFlyZones() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(NO_FLY_ZONE_URL)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), new TypeReference<>() {});
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