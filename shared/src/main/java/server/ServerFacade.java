package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade (String url) {
        serverUrl = url;
    }

    public AuthData register(UserData newUser) throws ResponseException {
        var request = buildRequestBody("POST", "/user", newUser);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData login(UserData user) {
        var request = buildRequestBody("POST", "/session", user);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(AuthData authData) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .DELETE()
                .setHeader("Authorization", authData.authToken())
                .build();
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public Map<String, List<GameData>> listGames(AuthData authData) {
        var request = buildRequestWithAuth("GET", "/game", authData, null);
        var response = sendRequest(request);

        Type mapType = new TypeToken<Map<String, List<GameData>>>(){}.getType();
        var status = response.statusCode();
        if (status / 100 != 2) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatus(status), "other failure: " + status);
        }
        return new Gson().fromJson(response.body(), mapType);
    }

    public GameData createGame(AuthData authData, String gameName) {
        Map<String, String> body = Map.of("gameName", gameName);
        var request = buildRequestWithAuth("POST", "/game", authData, body);
        var response = sendRequest(request);
        return handleResponse(response, GameData.class);
    }

    public void joinGame(AuthData authData, String playerColor, int gameId) {
        JsonObject body = new JsonObject();
        body.addProperty("playerColor", playerColor);
        body.addProperty("gameID", gameId);

        var request = buildRequestWithAuth("PUT", "/game", authData, body);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequestBody(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }

        return request.build();
    }

    private HttpRequest buildRequestWithAuth(String method, String path, AuthData authData, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));

        if (authData != null) {
            request.setHeader("Authorization", authData.authToken());
        }

        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.StatusCode.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) {
        var status = response.statusCode();
        if (status / 100 != 2) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatus(status), "other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }
}
