package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import dataaccess.SqlDataAccess;

import java.io.IOException;
import java.util.Objects;

public class WebsocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final SqlDataAccess dataAccess = new SqlDataAccess();


    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        System.out.print("Websocket closed");
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> make_move(ctx, command);
                case LEAVE -> leave(ctx, command);
                case RESIGN -> resign(ctx, command);
            }
        } catch (IOException e ) {
            System.out.print("Error: failed to parse command.");
        }
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) throws DataAccessException, IOException {
        String role;
        var session = ctx.session;
        var authData = dataAccess.getAuth(command.getAuthToken());
        var username = authData.username();
        var gameId = command.getGameID();
        var game = dataAccess.getGame(gameId);

        if (game.whiteUsername().equals(username)) {
            role = "white";
        } else if (game.blackUsername().equals(username)) {
            role = "black";
        } else {
            role = "observer";
        }
        connections.add(gameId, session, username);

        var loadGame = new LoadGameMessage(game.game());
        
        var message = String.format("%s joined the game as %s!", username, role);
        var notification = new NotificationMessage(message);
        connections.broadcast(session, notification, gameId);
    }

    private void make_move(WsMessageContext ctx, UserGameCommand command) {
        var session = ctx.session;

    }
}
