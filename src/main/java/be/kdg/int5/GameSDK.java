package be.kdg.int5;

import be.kdg.int5.domain.GameContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class GameSDK {
    protected final String apiKey;
    protected final String gameRegistryBaseUrl;
    protected final String statisticsBaseUrl;
    protected final int tokenExpirationMargin;

    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected String bearerToken;
    protected Instant tokenExpiresAt;

    private GameSDK(Builder builder, String apiKey) {
        this.gameRegistryBaseUrl = builder.gameRegistryBaseUrl;
        this.statisticsBaseUrl = builder.statisticsBaseUrl;
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

    public GameContext registerGame(
            String title,
            String hostUrl,
            String description,
            BigDecimal price,
            String iconUrl,
            String backgroundUrl
    ) {
        return RegisterGameModule.registerGame(
                this,
                title,
                hostUrl,
                description,
                price,
                iconUrl,
                backgroundUrl
        );
    }

    public boolean submitCompletedSession(
            GameContext ctx,
            UUID playerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            EndState endState,
            Integer turnsTaken,
            Double avgSecondsPerTurn,
            Integer playerScore,
            Integer opponentScore,
            Integer clicks,
            String character,
            Boolean wasFirstToGo
    ) {
        return SubmitCompletedSessionModule.submitCompletedSession(
                this,
                ctx,
                playerId,
                startTime,
                endTime,
                endState,
                turnsTaken,
                avgSecondsPerTurn,
                playerScore,
                opponentScore,
                clicks,
                character,
                wasFirstToGo
        );
    }

    public boolean updateAchievementProgress(
            GameContext ctx,
            UUID playerId,
            UUID achievementId,
            Integer newProgressAmount
    ) {
        return UpdateAchievementProgressModule.updateAchievementProgress(
                this,
                ctx,
                playerId,
                achievementId,
                newProgressAmount
        );
    }


    public static class Builder {
        private String gameRegistryBaseUrl = "http://localhost:8090/api";
        private String statisticsBaseUrl = "http://localhost:8090/api";
        private int tokenExpirationMargin = 10;

        private HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
        ;
        private ObjectMapper objectMapper = new ObjectMapper();

        public Builder baseUrl(String gameRegistryBaseUrl, String statisticsBaseUrl) {
            this.gameRegistryBaseUrl = gameRegistryBaseUrl;
            this.statisticsBaseUrl = statisticsBaseUrl;
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

    public static class GeneralMethodFailedException extends RuntimeException {
        public GeneralMethodFailedException() {
        }

        public GeneralMethodFailedException(String message) {
            super(message);
        }
    }

    public enum EndState {
        WIN, LOSS, DRAW
    }
}
