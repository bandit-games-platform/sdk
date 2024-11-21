package be.kdg.int5;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.Objects;

public class GameSDK {
    protected final String apiKey;
    protected final String baseUrl;
    protected final int tokenExpirationMargin;

    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected String bearerToken;
    protected Instant tokenExpiresAt;

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

    protected void authenticate() {
        AuthenticationModule.authenticate(this);
    }

    public String bearerToken() {
        if (bearerToken == null || isTokenExpired()) authenticate();
        return bearerToken;
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
