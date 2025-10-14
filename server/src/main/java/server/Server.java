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

            var authData = userService.register(user);
            ctx.result(serializer.toJson(authData));
        //create exception class and figure this out
        } catch (Exception ex) {
            var msg = String.format("error already taken");
            ctx.status(403).result(msg);
        }


    }

    private void login(Context ctx) {
//        var serializer = new Gson();
//        String reqJson = ctx.body();
//        var user = serializer.fromJson(reqJson, UserData.class);
//
//        var authData = userService.register(user);
//
//        var res = Map.of("username", req.get("username"), "authToken", "yzx");
//
//        ctx.result(serializer.toJson(res));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
