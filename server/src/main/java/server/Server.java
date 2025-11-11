package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.*;
import service.GameService;
import service.UserService;
import service.ErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        var dataAccess = new SqlDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", ctx -> clear(ctx));

        server.post("user", ctx -> register(ctx));

        server.post("session", ctx -> login(ctx));

        server.delete("session", ctx -> logout(ctx));

        server.post("game", ctx -> createGame(ctx));

        server.put("game", ctx -> joinGame(ctx));

        server.get("game", ctx -> listGames(ctx));

    }

    public void clear(Context ctx) {
        try {
            userService.clear();
            gameService.clear();

            ctx.status(200).result("{}");
        } catch (Exception e) {
            handleError(ctx, e, 500);
        }
    }

    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();

            var user = serializer.fromJson(reqJson, UserData.class);

            if (user.username() == null || user.password() == null || user.email() == null) {
                throw new BadRequestException("Error: bad request");
            }

            var authData = userService.register(user);
            ctx.status(200).result(serializer.toJson(authData));

        } catch (ErrorException ex) {
            handleError(ctx, ex, 403);
        } catch (BadRequestException e) {
            handleError(ctx, e, 400);
        } catch (DataAccessException x) {
            handleError(ctx, x, 500);
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            if (user.username() == null || user.password() == null) {
                throw new BadRequestException("Error: bad request");
            }

            var authData = userService.login(user);
            ctx.status(200).result(serializer.toJson(authData));

        } catch (BadRequestException e) {
            handleError(ctx, e, 400);
        } catch (ErrorException ex) {
            handleError(ctx, ex, 401);
        } catch (DataAccessException x) {
            handleError(ctx, x, 500);
        }
    }

    private void logout(Context ctx) throws ErrorException {
        try {
            String authData = ctx.header("authorization");

            if (authData == null) {
                throw new ErrorException("Error: unauthorized");
            }

            userService.logout(authData);
            ctx.status(200).result();
        } catch (ErrorException ex){
            handleError(ctx, ex, 401);
        } catch (DataAccessException x) {
            handleError(ctx, x, 500);
        }
    }

    private void createGame(Context ctx) throws ErrorException {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            String authData = ctx.header("authorization");
            GameRequest gameRequest = serializer.fromJson(reqJson, GameRequest.class);

            if (gameRequest.gameName() == null || gameRequest.gameName().isEmpty()) {
                throw new BadRequestException("Error: bad request");
            }

            var gameData = gameService.createGame(gameRequest.gameName(), authData);
            ctx.status(200).result(serializer.toJson(gameData));
        } catch (ErrorException ex) {
            handleError(ctx, ex, 401);
        } catch (BadRequestException e) {
            handleError(ctx, e, 400);
        } catch (Exception x) {
            handleError(ctx, x, 500);
        }
    }

    public void joinGame(Context ctx) throws ErrorException {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            String authData = ctx.header("authorization");
            JoinRequest joinReq = serializer.fromJson(reqJson, JoinRequest.class);

            if (joinReq.gameID() == null) {
                throw new BadRequestException("Error: bad request");
            }

            if ("WHITE".equals(joinReq.playerColor())|| "BLACK".equals(joinReq.playerColor())) {
                var joinResult = gameService.joinGame(joinReq, authData);
                ctx.status(200).result(serializer.toJson(joinResult));
            } else {
                throw new BadRequestException("Error: bad request");
            }

        } catch (BadRequestException ex) {
            handleError(ctx, ex, 400);
        } catch (GameService.GameTakenException e) {
            handleError(ctx, e, 403);
        } catch (ErrorException x) {
            handleError(ctx, x, 401);
        } catch (DataAccessException x) {
            handleError(ctx, x, 500);
        }
    }

    public void listGames(Context ctx) throws ErrorException {
        try {
            var serializer = new Gson();
            String authData = ctx.header("authorization");
            Map<String, Object> result = new HashMap<>();

            List<GameData> games = gameService.listGames(authData);
            result.put("games", games);

            ctx.status(200).result(serializer.toJson(result));

        } catch (ErrorException e) {
            handleError(ctx, e, 401);
        } catch (DataAccessException x) {
            handleError(ctx, x, 500);
        }
    }

    public void handleError(Context ctx, Exception ex, int statusCode) {
        var serializer = new Gson();
        HashMap<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("message", ex.getMessage());
        ctx.status(statusCode).result(serializer.toJson(error));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
