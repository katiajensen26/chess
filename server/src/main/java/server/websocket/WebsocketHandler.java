package server.websocket;

import chess.ChessMove;
import chess.ChessPosition;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WebsocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final SqlDataAccess dataAccess = new SqlDataAccess();
    private final Map<Integer, GameState> gameStates = new HashMap<>();


    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        System.out.print("Websocket closed\n");
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
                case LEAVE -> leave(ctx, command);
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
            gameStates.putIfAbsent(gameId, GameState.ACTIVE);

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

    private void makeMove(WsMessageContext ctx, UserGameCommand command) throws DataAccessException, IOException {
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

        if (Objects.equals(username, game.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (Objects.equals(username, game.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            var errorMessage = new ErrorMessage("You are not a player. You can't make a move.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }

        String opponentUsername;

        var opponent = (playerColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE);
        if (Objects.equals(opponent, ChessGame.TeamColor.WHITE)) {
            opponentUsername = game.whiteUsername();
        } else {
            opponentUsername = game.blackUsername();
        }

        GameState gameState = gameStates.get(game.gameID());
        if (!gameState.equals(GameState.ACTIVE)) {
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
            gameStates.put(game.gameID(), GameState.CHECKMATE);
            var notification = new NotificationMessage(message);
            connections.broadcast(null, notification, game.gameID());
        } else if (chessGame.isInStalemate(opponent)) {
            var message = String.format("%s is in stalemate!", opponentUsername);
            gameStates.put(game.gameID(), GameState.STALEMATE);
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

        var originalNotation = backToNotation(move);
        var message = String.format("%s made move: %s", username, originalNotation);
        var notification = new NotificationMessage(message);
        connections.broadcast(session, notification, game.gameID());

    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws DataAccessException, IOException {
        var session = ctx.session;
        var authData = dataAccess.getAuth(command.getAuthToken());
        var username = authData.username();
        var game = dataAccess.getGame(command.getGameID());

        GameState gameState = gameStates.get(game.gameID());
        if (gameState.equals(GameState.RESIGNED)) {
            var errorMessage = new ErrorMessage("Game has already ended. You can't resign.");
            connections.directSend(command.getGameID(), session, errorMessage);
            return;
        }

        if (username.equals(game.whiteUsername()) || username.equals(game.blackUsername())) {
            gameStates.put(game.gameID(), GameState.RESIGNED);
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

    private void leave(WsMessageContext ctx, UserGameCommand command) throws DataAccessException, IOException {
        var session = ctx.session;
        var authData = dataAccess.getAuth(command.getAuthToken());
        var username = authData.username();
        var game = dataAccess.getGame(command.getGameID());

        if (username.equals(game.whiteUsername())) {
            game = new GameData(game.gameID(),
                    null,
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    game.playerColor());
        } else if (username.equals(game.blackUsername())) {
            game = new GameData(game.gameID(),
                    game.whiteUsername(),
                    null,
                    game.gameName(),
                    game.game(),
                    game.playerColor());
        }

        dataAccess.updateGame(game);
        connections.remove(game.gameID(), session);
        var message = String.format("%s has left the game.", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(session, notification, game.gameID());
    }

    public enum GameState {
        ACTIVE,
        CHECKMATE,
        STALEMATE,
        RESIGNED
    }

    public String backToNotation(ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        var startCol = (char) ('a'+ start.getColumn());
        var endCol = (char) ('a' + end.getColumn());
        var startRow = 8 - start.getRow();
        var endRow = 8 - end.getRow();

        return "" + startCol + startRow + endCol + endRow;
    }
}
