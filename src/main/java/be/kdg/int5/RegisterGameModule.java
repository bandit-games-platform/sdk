package be.kdg.int5;

import be.kdg.int5.domain.GameContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

class RegisterGameModule {
    protected static GameContext registerGame(
            GameSDK sdk,
            String title,
            String hostUrl,
            String description,
            BigDecimal price,
            String iconUrl,
            String backgroundUrl
    ) {
        String json = "{";
        json += "\"title\": \""+Objects.requireNonNull(title)+"\",";
        json += "\"currentHost\": \""+Objects.requireNonNull(hostUrl)+"\",";

        if(description != null) json += "\"description\": \""+description+"\",";
        if(price != null) json += "\"currentPrice\": "+price.doubleValue()+",";
        if(iconUrl != null) json += "\"iconUrl\": \""+iconUrl+"\",";
        if(backgroundUrl != null) json += "\"backgroundUrl\": \""+backgroundUrl+"\",";

        json += "\"rules\": []";
        json += "}";

        HttpRequest request = HttpRequest.newBuilder()
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .setHeader("Authorization", "Bearer "+sdk.bearerToken())
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .uri(URI.create(sdk.baseUrl+"/registry/games"))
                .build();
        try {
            HttpResponse<String> response = sdk.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                if (response.statusCode() == 403) throw new GameSDK.AuthenticationFailedException();
                throw new GameSDK.GeneralMethodFailedException("Non-OK response status code: "+response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = sdk.objectMapper.readValue(response.body(), Map.class);
            if(!jsonMap.containsKey("uuid")) {
                throw new GameSDK.GeneralMethodFailedException("Got malformed response");
            }

            return new GameContext(UUID.fromString((String) jsonMap.get("uuid")));
        }catch (IOException | InterruptedException e) {
            throw new GameSDK.AuthenticationFailedException(e.getMessage());
        }
    }
}
