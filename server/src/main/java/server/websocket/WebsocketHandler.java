package server.websocket;

import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import dataaccess.SqlDataAccess;
import chess.ChessGame;

import java.io.IOException;

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
                case MAKE_MOVE -> makeMove(ctx, command);
//                case LEAVE -> leave(ctx, command);
//                case RESIGN -> resign(ctx, command);
            }
        } catch (IOException e ) {
            System.out.print("Error: failed to parse command.");
        }
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) throws IOException {
        String role;
        try {
            var session = ctx.session;
            var authData = dataAccess.getAuth(command.getAuthToken());
            if (authData == null) {
                var errorMessage = new ErrorMessage("User not registered.");
                connections.directSend(command.getGameID(), session, errorMessage);
            }
            var username = authData.username();
            var gameId = command.getGameID();
            var game = dataAccess.getGame(gameId);

            if (username.equals(game.whiteUsername())) {
                role = "white";
            } else if (username.equals(game.blackUsername())) {
                role = "black";
            } else {
                role = "observer";
            }
            connections.add(gameId, session, username);

            var loadGame = new LoadGameMessage(game.game());
            connections.directSend(gameId, session, loadGame);

            var message = String.format("%s joined the game as %s!", username, role);
            var notification = new NotificationMessage(message);
            connections.broadcast(session, notification, gameId);
        } catch (DataAccessException e) {
            var error = new ErrorMessage(e.getMessage());
            connections.directSend(command.getGameID(), ctx.session, error);
        }
    }

    private void makeMove(WsMessageContext ctx, UserGameCommand command) throws DataAccessException, IOException, InvalidMoveException {
        var session = ctx.session;
        var game = dataAccess.getGame(command.getGameID());
        ChessGame chessGame = game.game();
        var authData = dataAccess.getAuth(command.getAuthToken());
        var username = authData.username();
        var playerColor = ChessGame.TeamColor.WHITE;
        var move = command.getMove();

        if (username.equals(game.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(game.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            var errorMessage = new ErrorMessage("You are not a player. You can't make a move.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }

        if (playerColor.equals(chessGame.getTeamTurn())) {
            chessGame.makeMove(move);
        } else {
            var errorMessage = new ErrorMessage("It is not your turn.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }

        if (chessGame.isInStalemate(playerColor)) {
            var message = String.format("%s is in stalemate!", username);
            var notification = new NotificationMessage(message);
            connections.broadcast(null, notification, game.gameID());
        } else if (chessGame.isInCheck(playerColor)) {
            var message = String.format("%s is in check!", username);
            var notification = new NotificationMessage(message);
            connections.broadcast(null, notification, game.gameID());
        } else if (chessGame.isInCheckmate(playerColor)) {
            var message = String.format("%s is in checkmate!", username);
            var notification = new NotificationMessage(message);
            connections.broadcast(null, notification, game.gameID());
        }

        dataAccess.updateGame(game);

        var loadGameMessage = new LoadGameMessage(game.game());
        connections.broadcast(null, loadGameMessage, game.gameID());

        var message = String.format("%s made move: %s", username, move);
        var notification = new NotificationMessage(message);
        connections.broadcast(session, notification, game.gameID());

    }
}
