package be.kdg.int5;

import be.kdg.int5.domain.GameContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SubmitCompletedSessionModule {
    protected static boolean submitCompletedSession(
            GameSDK sdk,
            GameContext gameContext,
            UUID playerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            GameSDK.EndState endState,
            Integer turnsTaken,
            Double avgSecondsPerTurn,
            Integer playerScore,
            Integer opponentScore,
            Integer clicks,
            String character,
            Boolean wasFirstToGo
    ) {
        String json = """
            {
                "startTime": "%s",
                "endTime": "%s",
                "endState": "%s",
                "turnsTaken": "%s",
                "avgSecondsPerTurn": "%s",
                "playerScore": "%s",
                "opponentScore": "%s",
                "clicks": "%s",
                "character": "%s",
                "wasFirstToGo": "%s"
            }
            """
            .formatted(
                    Objects.requireNonNull(startTime.toString()),
                    Objects.requireNonNull(endTime.toString()),
                    Objects.requireNonNull(endState),
                    turnsTaken,
                    avgSecondsPerTurn,
                    playerScore,
                    opponentScore,
                    clicks,
                    character,
                    wasFirstToGo
            );

        HttpRequest request = HttpRequest.newBuilder()
                .method("POST", HttpRequest.BodyPublishers.ofString(json))
                .setHeader("Authorization", "Bearer "+sdk.bearerToken())
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(sdk.statisticsBaseUrl+"/statistics/submit?playerId=" + playerId + "&gameId=" + gameContext.gameId()))
                .build();
        try {
            HttpResponse<String> response = sdk.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                if (response.statusCode() == 403) throw new GameSDK.AuthenticationFailedException();
                throw new GameSDK.GeneralMethodFailedException("Something went wrong with the request: "+response.statusCode());
            }
            return true;
        }catch (IOException | InterruptedException e) {
            throw new GameSDK.GeneralMethodFailedException(e.getMessage());
        }
    }
}
