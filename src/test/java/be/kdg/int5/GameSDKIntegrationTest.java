package be.kdg.int5;

import be.kdg.int5.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameSDKIntegrationTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void invalidApiKeyShouldResultInAuthenticationFailedException() {
        //Arrange

        //Act
        Executable test = () -> new GameSDK.Builder().init("invalidkey");

        //Assert
        assertThrows(GameSDK.AuthenticationFailedException.class, test);
    }

    @Test
    void revokedApiKeyShouldResultInAuthenticationFailedException() {
        //Arrange
        // The backend database already contains a revoked api key: band1TAAA

        //Act
        Executable test = () -> new GameSDK.Builder().init("band1TAAA");

        //Assert
        assertThrows(GameSDK.AuthenticationFailedException.class, test);
    }

    @Test
    void validApiKeyShouldObtainBearerTokenOnInit() {
        //Arrange
        // The backend database already contains a valid api key: band1TBBB

        //Act
        GameSDK sdk = new GameSDK.Builder().init("band1TCCC");

        //Assert
        assertFalse(sdk.isTokenExpired());

        assertFalse(sdk.bearerToken().isEmpty());

        System.out.println("Token: "+sdk.bearerToken());
    }

    @Test
    void registerGameShouldCreateGameAndReturnGameContext() {
        //Arrange
        GameSDK sdk = new GameSDK.Builder().init("band1TBBB");

        //Act
        GameContext ctx = sdk.registerGame(
                "Duck!",
                "http://localhost:4242/duckgame",
                "Duck! is the world class duck game where you must duck.",
                BigDecimal.valueOf(0.42),
                "http://localhost:4242/assets/duckgame/icon.svg",
                "http://localhost:4242/assets/duckgame/cover.png",
                Arrays.asList(
                        new Rule(1, "You are a duck."),
                        new Rule(2, "You must duck!")
                ),
                List.of("http://localhost:4242/assets/duckgame/screenshot1.png"),
                Arrays.asList(
                        new Achievement(101, "Ducking Duck!", "Duck for the first time."),
                        new Achievement(240, "Is It Still Breathing?", "Stay underwater for over a minute.", 60)
                )
        );

        //Assert
        assertNotNull(ctx.gameId());

        System.out.println("GameId: "+ctx.gameId());
    }

    @Test
    void createLobbyShouldReturnLobbyContextOnSuccess() {
        //Arrange
        GameSDK sdk = new GameSDK.Builder().init("band1TBBB");
        GameContext ctx = sdk.registerGame(
                "Lobby Test Game",
                "",
                "",
                null,
                "",
                "",
                new ArrayList<>(),
                null,
                null
        );
        assertNotNull(ctx.gameId());

        //Act
        LobbyContext lobby = sdk.createLobby(
                ctx,
                UUID.fromString("9f01b00e-e627-497c-975c-452451cc0b55"),
                2
        );

        //Assert
        assertNotNull(lobby.lobbyId());

        System.out.println("LobbyId: "+lobby.lobbyId());
    }

    @Test
    void patchLobbyShouldThrowOnNonExistentLobby() {
        //Arrange
        GameSDK sdk = new GameSDK.Builder().init("band1TBBB");

        LobbyContext nonExistentLobby = new LobbyContext(UUID.randomUUID());
        //Act
        Executable test = () -> sdk.patchLobby(nonExistentLobby, null, 2, true);

        //Assert
        assertThrows(GameSDK.GeneralMethodFailedException.class, test);
    }

    @Test
    void patchLobbyShouldNotThrowOnExistentLobby() {
        //Arrange
        GameSDK sdk = new GameSDK.Builder().init("band1TBBB");
        GameContext ctx = sdk.registerGame(
                "Lobby Test Game",
                "",
                "",
                null,
                "",
                "",
                new ArrayList<>(),
                null,
                null
        );
        assertNotNull(ctx.gameId());

        LobbyContext lobby = sdk.createLobby(
                ctx,
                UUID.fromString("9f01b00e-e627-497c-975c-452451cc0b55"),
                2
        );
        assertNotNull(lobby.lobbyId());
        //Act
        Executable test = () -> sdk.patchLobby(lobby, null, 2, true);

        //Assert
        assertDoesNotThrow(test);
    }

    @Test
    void addingSessionForPlayerForGameShouldSucceed() {
        //Arrange
        GameSDK sdk = new GameSDK.Builder().init("band1TCCC");
        GameContext ctx = new GameContext(UUID.fromString("d77e1d1f-6b46-4c89-9290-3b9cf8a7c001"));
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now().minusMinutes(20);

        //Act
        boolean added = sdk.submitCompletedSession(
                ctx,
                UUID.fromString("94dad160-f5c8-4817-8f2d-611e1436ffcd"),
                startTime,
                endTime,
                EndState.WIN,
                20,
                null,
                null,
                null,
                null,
                null,
                null
        );

        //Assert
        assertNotNull(ctx.gameId());
        assertTrue(added);
    }

    @Test
    void updatingAchievementProgressForPlayerForGameShouldSucceedWithNoProgressAmount() {
        //Arrange
        GameSDK sdk = new GameSDK.Builder().init("band1TCCC");
        GameContext ctx = new GameContext(UUID.fromString("d77e1d1f-6b46-4c89-9290-3b9cf8a7c001"));

        //Act
        boolean updated = sdk.updateAchievementProgress(
                ctx,
                UUID.fromString("94dad160-f5c8-4817-8f2d-611e1436ffcd"),
                1,
                null
        );

        //Assert
        assertTrue(updated);
    }

    @Test
    void updatingAchievementProgressForPlayerForGameShouldSucceedWithSetProgressAmount() {
        //Arrange
        GameSDK sdk = new GameSDK.Builder().init("band1TCCC");
        GameContext ctx = new GameContext(UUID.fromString("d77e1d1f-6b46-4c89-9290-3b9cf8a7c001"));

        //Act
        boolean updated = sdk.updateAchievementProgress(
                ctx,
                UUID.fromString("94dad160-f5c8-4817-8f2d-611e1436ffcd"),
                2,
                10
        );

        //Assert
        assertTrue(updated);
    }
}
