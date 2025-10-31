package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    private final DataAccess dataAccess;
    private int gameID = 1;


    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws ErrorException, DataAccessException {
        try {
            dataAccess.clear();
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }

    public GameData createGame(GameData newGame, String authToken) throws ErrorException, DataAccessException {
        try {
            AuthData storedAuth = dataAccess.getAuth(authToken);

            if (storedAuth == null) {
                throw new ErrorException("Error: unauthorized");
            }


            int gameID = newGameID();
            GameData gameToCreate = new GameData(gameID,
                    newGame.whiteUsername(),
                    newGame.blackUsername(),
                    newGame.gameName(),
                    newGame.game(),
                    newGame.playerColor());
            dataAccess.createGame(gameToCreate);

            return gameToCreate;
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }


    public GameData joinGame(GameData gameRequest, String authToken) throws ErrorException, DataAccessException {
        try {
            AuthData storedAuth = dataAccess.getAuth(authToken);

            if (storedAuth == null) {
                throw new ErrorException("Error: unauthorized");
            }

            GameData requestedGame = dataAccess.getGame(gameRequest.gameID());

            if ("WHITE".equals(gameRequest.playerColor())) {
                if (requestedGame.whiteUsername() != null) {
                    throw new GameTakenException("Error: Game already taken");
                }
                GameData updatedGame = new GameData(requestedGame.gameID(),
                        storedAuth.username(),
                        requestedGame.blackUsername(),
                        requestedGame.gameName(),
                        requestedGame.game(),
                        "WHITE");
                return dataAccess.updateGame(updatedGame);
            } else {
                if (requestedGame.blackUsername() != null) {
                    throw new GameTakenException("Error: Game already taken");
                }
                GameData updatedGame = new GameData(requestedGame.gameID(),
                        requestedGame.whiteUsername(),
                        storedAuth.username(),
                        requestedGame.gameName(),
                        requestedGame.game(),
                        "BLACK");
                return dataAccess.updateGame(updatedGame);
            }
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }

    public List<GameData> listGames(String authToken) throws ErrorException, DataAccessException {
        try {
            AuthData storedAuth = dataAccess.getAuth(authToken);

            if (storedAuth == null) {
                throw new ErrorException("Error: unauthorized");
            }

            return dataAccess.getGames();
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }

    public int newGameID() {
        return gameID++;
    }

    public static class GameTakenException extends ErrorException {
        public GameTakenException(String message) {
            super(message);
        }
    }
}
