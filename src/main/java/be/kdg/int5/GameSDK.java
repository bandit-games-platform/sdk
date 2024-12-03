package be.kdg.int5;

import be.kdg.int5.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GameSDK {
    protected final String apiKey;
    protected final String gameRegistryBaseUrl;
    protected final String statisticsBaseUrl;
    protected final String gameplayBaseUrl;
    protected final int tokenExpirationMargin;

    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected String bearerToken;
    protected Instant tokenExpiresAt;

    private GameSDK(Builder builder, String apiKey) {
        this.gameRegistryBaseUrl = builder.gameRegistryBaseUrl;
        this.statisticsBaseUrl = builder.statisticsBaseUrl;
        this.gameplayBaseUrl = builder.gameplayBaseUrl;
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
            String backgroundUrl,
            List<Rule> rules,
            List<String> screenshots,
            List<Achievement> achievements
    ) {
        return RegisterGameModule.registerGame(
                this,
                title,
                hostUrl,
                description,
                price,
                iconUrl,
                backgroundUrl,
                rules,
                screenshots,
                achievements
        );
    }

    public LobbyContext createLobby(GameContext ctx, UUID ownerId, int maxPlayers) {
        return LobbyModule.createLobby(this, ctx, ownerId, maxPlayers);
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
            int achievementNumber,
            Integer newProgressAmount
    ) {
        return UpdateAchievementProgressModule.updateAchievementProgress(
                this,
                ctx,
                playerId,
                achievementNumber,
                newProgressAmount
        );
    }


    public static class Builder {
        private String gameRegistryBaseUrl = "http://localhost:8090/api";
        private String statisticsBaseUrl = "http://localhost:8090/api";
        private String gameplayBaseUrl = "http://localhost:8090/api";
        private int tokenExpirationMargin = 10;

        private HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
        ;
        private ObjectMapper objectMapper = new ObjectMapper();

        /**
         * Will set all context specific base urls to <b>a single general base url</b> (useful when hosted as monolith).
         * <br><br>
         * Use the context specific builder methods when finer control is desired. (i.e. when hosted as microservices).
         * @param generalBaseUrl The general url all the contexts are hosted on
         * @return the builder
         * @see #gameRegistryBaseUrl(String)
         * @see #statisticsBaseUrl(String)
         * @see #gameplayBaseUrl(String)
         */
        public Builder baseUrl(String generalBaseUrl) {
            this.gameRegistryBaseUrl = generalBaseUrl;
            this.statisticsBaseUrl = generalBaseUrl;
            this.gameplayBaseUrl = generalBaseUrl;
            return this;
        }

        public Builder gameRegistryBaseUrl(String baseUrl) {
            this.gameRegistryBaseUrl = baseUrl;
            return this;
        }

        public Builder statisticsBaseUrl(String baseUrl) {
            this.statisticsBaseUrl = baseUrl;
            return this;
        }

        public Builder gameplayBaseUrl(String baseUrl) {
            this.gameRegistryBaseUrl = baseUrl;
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
}
