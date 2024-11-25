package be.kdg.int5;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;

class AuthenticationModule {
    protected static void authenticate(GameSDK sdk) {
        sdk.bearerToken = null;

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"apiKey\":\""+sdk.apiKey+"\"}"))
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .uri(URI.create(sdk.gameRegistryBaseUrl+"/registry/auth"))
                .build();
        try {
            HttpResponse<String> response = sdk.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                throw new GameSDK.AuthenticationFailedException("Non-OK response status code: "+response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = sdk.objectMapper.readValue(response.body(), Map.class);
            if(!jsonMap.containsKey("access_token") || !jsonMap.containsKey("expires_in")) {
                throw new GameSDK.AuthenticationFailedException("Got malformed response");
            }

            sdk.bearerToken = (String) jsonMap.get("access_token");
            sdk.tokenExpiresAt = Instant.now().plusSeconds(((int) jsonMap.get("expires_in")) - sdk.tokenExpirationMargin);
        }catch (IOException | InterruptedException e) {
            throw new GameSDK.AuthenticationFailedException(e.getMessage());
        }
    }
}
