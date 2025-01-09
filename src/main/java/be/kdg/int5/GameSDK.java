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

    public void patchLobby(LobbyContext lobby, UUID ownerId, Integer playerCount, Boolean closed) {
        LobbyModule.patchLobby(this, lobby, ownerId, playerCount, closed);
    }

    public void changeLobbyOwner(LobbyContext lobby, UUID newOwnerId) {
        patchLobby(lobby, newOwnerId, null, null);
    }

    public void updateLobbyPlayerCount(LobbyContext lobby, Integer playerCount) {
        patchLobby(lobby, null, playerCount, null);
    }

    /**
     * Closes the specified lobby, this is <b>meant to be called when the game is being started</b> (i.e. the lobby finished queueing).
     * <br><br>
     * Closed lobbies do not allow the owner to invite any more players and outstanding invites cannot be accepted until reopened.
     * @param lobby the lobby context that identifies the lobby being closed
     */
    public void closeLobby(LobbyContext lobby) {
        patchLobby(lobby, null, null, true);
    }

    /**
     * Opens the specified lobby, you can call this method to reopen a lobby for queueing (e.g. after a match has concluded).
     * <br><br>
     * <i>Note: You do not need to call this method after creating a fresh lobby (new lobbies are open by default)</i>
     * @param lobby the lobby context that identifies the lobby being reopened
     */
    public void openLobby(LobbyContext lobby) {
        patchLobby(lobby, null, null, false);
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
        private String gameRegistryBaseUrl = "https://game-registry-prod-container.blackwave-a5cb5824.northeurope.azurecontainerapps.io/game-registry";
        private String statisticsBaseUrl = "https://statistics-prod-container.blackwave-a5cb5824.northeurope.azurecontainerapps.io/statistics";
        private String gameplayBaseUrl = "https://gameplay-prod-container.blackwave-a5cb5824.northeurope.azurecontainerapps.io/gameplay";
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
            this.gameplayBaseUrl = baseUrl;
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
