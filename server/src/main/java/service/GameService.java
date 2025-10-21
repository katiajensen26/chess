package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;

public class GameService {
    private final DataAccess dataAccess;
    private int gameID = 1;


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

        return new GameData(gameID, newGame.whiteUsername(), newGame.blackUsername(), newGame.gameName(), new ChessGame());
    }


    public int newGameID() {
        return gameID++;
    }
}
