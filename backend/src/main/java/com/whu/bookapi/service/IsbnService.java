package com.whu.bookapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class IsbnService {

    private static final String API_KEY = "9ea14df1c6a17986416da0956592bde3";
    private static final String API_URL = "http://apis.juhe.cn/isbn/query";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IsbnService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, String> getBookInfo(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be empty");
        }

        try {
            // Encode parameters as per the example
            String encodedKey = URLEncoder.encode(API_KEY, StandardCharsets.UTF_8);
            String encodedIsbn = URLEncoder.encode(isbn, StandardCharsets.UTF_8);
            
            String fullUrl = API_URL + "?key=" + encodedKey + "&isbn=" + encodedIsbn;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            // Log raw response for debugging
            System.out.println("Juhe ISBN API Response: " + body);

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(body);

                // Check error_code
                if (root.has("error_code") && root.get("error_code").asInt() != 0) {
                     String msg = root.has("reason") ? root.get("reason").asText() : "Unknown error";
                     // Log the specific error to help user debug
                     System.err.println("Juhe API Error: " + msg);
                     throw new RuntimeException("ISBN API returned error: " + msg);
                }
                
                JsonNode dataNode = root.get("result");
                if (dataNode == null || dataNode.isNull()) {
                     throw new RuntimeException("No result found in response");
                }
                
                // Handle case where result contains "data" object (nested structure)
                if (dataNode.has("data") && dataNode.get("data").isObject()) {
                    dataNode = dataNode.get("data");
                }

                Map<String, String> result = new HashMap<>();

                // Parse fields - prioritize 'title' as it is standard in Juhe
                if (dataNode.has("title")) result.put("bookName", dataNode.get("title").asText());
                else if (dataNode.has("subname")) result.put("bookName", dataNode.get("subname").asText());
                
                if (dataNode.has("author")) result.put("author", dataNode.get("author").asText());
                
                // Cover image
                if (dataNode.has("images_large") && !dataNode.get("images_large").asText().isEmpty()) {
                    result.put("coverUrl", dataNode.get("images_large").asText());
                } else if (dataNode.has("images_medium")) {
                    result.put("coverUrl", dataNode.get("images_medium").asText());
                } else if (dataNode.has("img")) {
                    result.put("coverUrl", dataNode.get("img").asText());
                }
                
                // Description
                if (dataNode.has("summary")) result.put("description", dataNode.get("summary").asText());
                else if (dataNode.has("catalog")) result.put("description", dataNode.get("catalog").asText());
                
                if (dataNode.has("publisher")) result.put("publishing", dataNode.get("publisher").asText());
                
                if (dataNode.has("price")) result.put("price", dataNode.get("price").asText());

                if (result.isEmpty()) {
                     throw new RuntimeException("Parsed result is empty from body: " + body);
                }

                System.out.println("Parsed ISBN Info: " + result);
                return result;
            } else {
                throw new RuntimeException("ISBN API request failed with status: " + response.statusCode() + ", body: " + body);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching ISBN info: " + e.getMessage(), e);
        }
    }
}
