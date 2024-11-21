package be.kdg.int5.domain;

import java.util.Objects;
import java.util.UUID;

public class GameContext {
    private final UUID gameId;

    protected GameContext(UUID gameId) {
        this.gameId = Objects.requireNonNull(gameId);
    }

    public UUID getGameId() {
        return gameId;
    }
}
