package be.kdg.int5;

import be.kdg.int5.domain.GameContext;
import be.kdg.int5.domain.LobbyContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

class LobbyModule {
    protected static LobbyContext createLobby(GameSDK sdk, GameContext ctx, UUID ownerId, int maxPlayers) {
        String json = "{";
        json += "\"gameId\": \""+ctx.gameId().toString()+"\", ";
        json += "\"ownerId\": \""+ownerId.toString()+"\", ";
        json += "\"maxPlayers\": "+maxPlayers;
        json += "}";

        HttpRequest request = HttpRequest.newBuilder()
                .method("POST", HttpRequest.BodyPublishers.ofString(json))
                .setHeader("Authorization", "Bearer "+sdk.bearerToken())
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .uri(URI.create(sdk.gameplayBaseUrl+"/lobby"))
                .build();
        try {
            HttpResponse<String> response = sdk.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 201) {
                if (response.statusCode() == 403) throw new GameSDK.AuthenticationFailedException();
                throw new GameSDK.GeneralMethodFailedException("Non-OK response status code: "+response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = sdk.objectMapper.readValue(response.body(), Map.class);
            if(!jsonMap.containsKey("uuid")) {
                throw new GameSDK.GeneralMethodFailedException("Got malformed response");
            }

            return new LobbyContext(UUID.fromString((String) jsonMap.get("uuid")));
        }catch (IOException | InterruptedException e) {
            throw new GameSDK.GeneralMethodFailedException(e.getMessage());
        }
    }
}
