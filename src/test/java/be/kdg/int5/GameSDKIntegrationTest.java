package be.kdg.int5;

import be.kdg.int5.domain.GameContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.UUID;

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
                null,
                "http://localhost:4242/assets/duckgame/icon.svg",
                "http://localhost:4242/assets/duckgame/cover.png"
        );

        //Assert
        assertNotNull(ctx.gameId());

        System.out.println("GameId: "+ctx.gameId());
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
                GameSDK.EndState.WIN,
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
                UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
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
                UUID.fromString("123e4567-e89b-12d3-a456-426614174002"),
                10
        );

        //Assert
        assertTrue(updated);
    }
}
