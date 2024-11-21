package be.kdg.int5.domain;

import java.util.Objects;
import java.util.UUID;

public record GameContext(UUID gameId) {
    public GameContext {
        Objects.requireNonNull(gameId);
    }
}
