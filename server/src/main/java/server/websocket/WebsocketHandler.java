package server.websocket;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import model.GameData;
import org.jetbrains.annotations.NotNull;
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
                case RESIGN -> resign(ctx, command);
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
        if (authData == null) {
            var errorMessage = new ErrorMessage("User not registered.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }
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

        String opponentUsername;

        var opponent = (playerColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE);
        if (opponent.equals(ChessGame.TeamColor.WHITE)) {
            opponentUsername = game.whiteUsername();
        } else {
            opponentUsername = game.blackUsername();
        }

        if (game.whiteUsername().equals("RESIGNED") || game.blackUsername().equals("RESIGNED")) {
            var errorMessage = new ErrorMessage("Game is over. No moves can be made.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }

        if (playerColor.equals(chessGame.getTeamTurn())) {
            try {
                chessGame.makeMove(move);
            } catch (InvalidMoveException e) {
                var errorMessage = new ErrorMessage("Invalid move.");
                connections.directSend(command.getGameID(), session, errorMessage);
                return;
            }
        } else {
            var errorMessage = new ErrorMessage("It is not your turn.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }

        if (chessGame.isInCheckmate(opponent)) {
            var message = String.format("%s is in checkmate!", opponentUsername);
            var notification = new NotificationMessage(message);
            connections.broadcast(null, notification, game.gameID());
        } else if (chessGame.isInStalemate(opponent)) {
            var message = String.format("%s is in stalemate!", opponentUsername);
            var notification = new NotificationMessage(message);
            connections.broadcast(null, notification, game.gameID());
        } else if (chessGame.isInCheck(opponent)) {
            var message = String.format("%s is in check!", opponentUsername);
            var notification = new NotificationMessage(message);
            connections.broadcast(null, notification, game.gameID());
        }

        game = new GameData(game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                chessGame,
                game.playerColor());

        dataAccess.updateGame(game);

        var loadGameMessage = new LoadGameMessage(game.game());
        connections.broadcast(null, loadGameMessage, game.gameID());

        var message = String.format("%s made move: %s", username, move);
        var notification = new NotificationMessage(message);
        connections.broadcast(session, notification, game.gameID());

    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws DataAccessException, IOException {
        var session = ctx.session;
        var authData = dataAccess.getAuth(command.getAuthToken());
        var username = authData.username();
        var game = dataAccess.getGame(command.getGameID());

        if (username.equals(game.whiteUsername())) {
            game = new GameData(game.gameID(),
                    "RESIGNED",
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    game.playerColor());
        } else if (username.equals(game.blackUsername())) {
            game = new GameData(game.gameID(),
                    game.whiteUsername(),
                    "RESIGNED",
                    game.gameName(),
                    game.game(),
                    game.playerColor());
        } else {
            var errorMessage = new ErrorMessage("Sorry, you can't resign.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }

        dataAccess.updateGame(game);

        var message = String.format("%s has resigned. Game is over!", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(null, notification, command.getGameID());
    }
}
