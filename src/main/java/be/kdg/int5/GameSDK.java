package be.kdg.int5;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class GameSDK {
    private final String apiKey;
    private final String baseUrl;
    private final int tokenExpirationMargin;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String bearerToken;
    private Instant tokenExpiresAt;

    private GameSDK(Builder builder, String apiKey) {
        this.baseUrl = builder.baseUrl;
        this.tokenExpirationMargin = builder.tokenExpirationMargin;
        this.httpClient = builder.httpClient;
        this.objectMapper = builder.objectMapper;
        this.apiKey = Objects.requireNonNull(apiKey);
        authenticate();
    }

    protected boolean isTokenExpired() {
        return Instant.now().isAfter(tokenExpiresAt);
    }

    public String bearerToken() {
        if (bearerToken == null || isTokenExpired()) authenticate();

        return bearerToken;
    }

    protected void authenticate() {
        this.bearerToken = null;

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"apiKey\":\""+apiKey+"\"}"))
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .uri(URI.create(baseUrl+"/registry/auth"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                throw new AuthenticationFailedException();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = objectMapper.readValue(response.body(), Map.class);
            if(!jsonMap.containsKey("access_token") || !jsonMap.containsKey("expires_in")) {
                throw new AuthenticationFailedException();
            }

            this.bearerToken = (String) jsonMap.get("access_token");
            this.tokenExpiresAt = Instant.now().plusSeconds(((int) jsonMap.get("expires_in")) - tokenExpirationMargin);
        }catch (IOException | InterruptedException e) {
            throw new AuthenticationFailedException();
        }
    }

    public static class Builder {
        private String baseUrl = "http://localhost:8090/api";
        private int tokenExpirationMargin = 10;

        private HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
        ;
        private ObjectMapper objectMapper = new ObjectMapper();

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * @param marginInSeconds how many seconds before the actual expiration time the bearer token should be considered expired (this is a safety margin)
         * @return the builder
         */
        public Builder tokenExpirationMargin(int marginInSeconds) {
            this.tokenExpirationMargin = marginInSeconds;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public GameSDK init(String apiKey) {
            return new GameSDK(this, apiKey);
        }
    }

    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException() {
        }

        public AuthenticationFailedException(String message) {
            super(message);
        }
    }
}
