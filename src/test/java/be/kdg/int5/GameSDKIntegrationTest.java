package be.kdg.int5;

import be.kdg.int5.domain.GameContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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
        GameSDK sdk = new GameSDK.Builder().init("band1TBBB");

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
}
