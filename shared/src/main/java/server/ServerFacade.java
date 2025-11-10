package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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

    public List<GameData> listGames(AuthData authData) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .GET()
                .setHeader("Authorization", authData.authToken())
                .build();
        var response = sendRequest(request);

        if (response.statusCode() / 100 != 2) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatus(response.statusCode()), "other failure: " + response.statusCode());
        }

        Type listType = new TypeToken<List<GameData>>(){}.getType();
        return new Gson().fromJson(response.body(), listType);
    }

    public GameData createGame(AuthData authData) {
        var request = buildRequestBody("POST", "/game", authData);
        var response = sendRequest(request);
        return handleResponse(response, GameData.class);
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
