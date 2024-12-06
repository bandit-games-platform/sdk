package be.kdg.int5.domain;

import java.util.Objects;
import java.util.UUID;

public record LobbyContext(UUID lobbyId) {
    public LobbyContext {
        Objects.requireNonNull(lobbyId);
    }
}
