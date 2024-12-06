package be.kdg.int5;

import be.kdg.int5.domain.GameContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

class UpdateAchievementProgressModule {
    protected static boolean updateAchievementProgress(
            GameSDK sdk,
            GameContext gameContext,
            UUID playerId,
            int achievementNumber,
            Integer newProgressAmount
    ) {
        String json = """
            {
                "newProgressAmount": "%s"
            }
            """
                .formatted(
                        newProgressAmount
                );

        HttpRequest request = HttpRequest.newBuilder()
                .method("POST", HttpRequest.BodyPublishers.ofString(json))
                .setHeader("Authorization", "Bearer "+sdk.bearerToken())
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(sdk.statisticsBaseUrl+"/statistics/achievements/" + achievementNumber + "?playerId=" + playerId + "&gameId=" + gameContext.gameId()))
                .build();
        try {
            HttpResponse<String> response = sdk.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                if (response.statusCode() == 403) throw new GameSDK.AuthenticationFailedException();
                throw new GameSDK.GeneralMethodFailedException("Request sending failed: "+response.statusCode());
            }
            return true;
        }catch (IOException | InterruptedException e) {
            throw new GameSDK.GeneralMethodFailedException(e.getMessage());
        }
    }
}
