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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class BaiduOcrService {

    private static final String API_KEY = "kvjtTqRK5OIQQ7hXBQ8V2G5J";
    private static final String SECRET_KEY = "TdsHFioXfqcpV99wb0OD3ytWj42LjX6k";
    
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private long tokenExpiresAt = 0;

    public BaiduOcrService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < tokenExpiresAt) {
            return accessToken;
        }

        try {
            String url = TOKEN_URL + "?grant_type=client_credentials" +
                    "&client_id=" + API_KEY +
                    "&client_secret=" + SECRET_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("access_token")) {
                    accessToken = root.get("access_token").asText();
                    // expires_in is in seconds, reduce by 60s for safety
                    int expiresIn = root.get("expires_in").asInt();
                    tokenExpiresAt = now + (expiresIn - 60) * 1000L;
                    return accessToken;
                } else {
                    throw new RuntimeException("Failed to get access token: " + response.body());
                }
            } else {
                throw new RuntimeException("Failed to get access token, status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error getting access token", e);
        }
    }

    public List<String> recognizeText(byte[] imageBytes) {
        try {
            String token = getAccessToken();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String encodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8);
            
            String body = "image=" + encodedImage;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OCR_URL + "?access_token=" + token))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                List<String> results = new ArrayList<>();
                if (root.has("words_result")) {
                    for (JsonNode node : root.get("words_result")) {
                        results.add(node.get("words").asText());
                    }
                }
                return results;
            } else {
                throw new RuntimeException("OCR request failed: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error performing OCR", e);
        }
    }
}
