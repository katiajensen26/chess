package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;

public class GameService {
    private final DataAccess dataAccess;
    private int gameID = 0;


    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() {
        dataAccess.clear();
    }

    public GameData createGame(GameData newGame, String authToken) throws ErrorException {
        AuthData storedAuth = dataAccess.getAuth(authToken);

        if (storedAuth == null) {
            throw new ErrorException("Error: unauthorized");
        }

        int gameID = newGameID();
        dataAccess.createGame(newGame);

        return new GameData(gameID, newGame.whiteUsername(), newGame.blackUsername(), newGame.gameName(), new ChessGame(), newGame.playerColor());
    }


    public GameData joinGame(GameData gameRequest, String authToken) throws ErrorException {
        AuthData storedAuth = dataAccess.getAuth(authToken);

        if (storedAuth == null) {
            throw new ErrorException("Error: unauthorized");
        }

        GameData requestedGame = dataAccess.getGame(gameRequest.gameID());

        if ("WHITE".equals(gameRequest.playerColor())) {
            if (requestedGame.whiteUsername() != null) {throw new ErrorException("Error: Game already taken");}
            GameData updatedGame = new GameData(requestedGame.gameID(), storedAuth.username(), requestedGame.blackUsername(), requestedGame.gameName(), requestedGame.game(), "WHITE");
            return dataAccess.updateGame(updatedGame);
        } else {
            if (requestedGame.blackUsername() != null) {throw new ErrorException("Error: Game already taken");}
            GameData updatedGame = new GameData(requestedGame.gameID(), requestedGame.whiteUsername(), storedAuth.username(), requestedGame.gameName(), requestedGame.game(), "BLACK");
            return dataAccess.updateGame(updatedGame);
        }

    }

    public int newGameID() {
        return gameID++;
    }
}
