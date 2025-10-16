package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import model.*;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", ctx -> ctx.result("{}"));

        server.post("user", ctx -> register(ctx));

        server.post("session", ctx -> login(ctx));

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
        } catch (UserService.AlreadyTakenException ex) {
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

            if (user.username() == null || user.password() == null || user.email() == null) {
                throw new BadRequestException("Error: bad request");
            }

            var authData = userService.login(user);
            ctx.status(200).result(serializer.toJson(authData));

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
