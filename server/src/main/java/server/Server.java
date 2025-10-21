package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import model.*;
import service.GameService;
import service.UserService;
import service.ErrorException;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", ctx -> ctx.result("{}"));

        server.post("user", ctx -> register(ctx));

        server.post("session", ctx -> login(ctx));

        server.delete("session", ctx -> logout(ctx));

        server.post("game", ctx -> createGame(ctx));

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

        //create exception class and figure this out
        } catch (ErrorException ex) {
            ctx.status(403).result(ex.getMessage());
        } catch (BadRequestException e) {
            ctx.status(400).result(e.getMessage());
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
            ctx.status(400).result(e.getMessage());
        } catch (ErrorException ex) {
            ctx.status(401).result(ex.getMessage());
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
            ctx.status(401).result(ex.getMessage());
        }
    }

    private void createGame(Context ctx) throws ErrorException {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            String authData = ctx.header("authorization");
            GameData newGame = serializer.fromJson(reqJson, GameData.class);

            if (newGame.gameName() == null) {
                throw new BadRequestException("Error: bad request");
            }

            var gameData = gameService.createGame(newGame, authData);
            ctx.status(200).result(serializer.toJson(gameData));
        } catch (ErrorException ex) {
            ctx.status(401).result(ex.getMessage());
        } catch (BadRequestException e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
