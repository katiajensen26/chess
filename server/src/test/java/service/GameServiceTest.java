package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    @Test
    void createGame() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game = new GameData(0, null, null, "GhessGame", null, null);
        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        var gameData = gameService.createGame(game, authData.authToken());
        var storedGame = db.getGame(gameData.gameID());

        assertNotNull(storedGame);
        assertEquals(gameData.gameID(), storedGame.gameID());
        assertEquals(gameData.gameName(), storedGame.gameName());
    }

    @Test
    void createGameUnauthorized() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game = new GameData(0, null, null, "GhessGame", null, null);
        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        userService.logout(authData.authToken());

        assertThrows(ErrorException.class, () -> gameService.createGame(game, authData.authToken()));
    }



    @Test
    void joinGameSuccessWhite() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game = new GameData(0, null, null, "GhessGame", null, null);
        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        var gameData = gameService.createGame(game, authData.authToken());
        GameData joinRequest = new GameData(gameData.gameID(), null, null, gameData.gameName(), null, "WHITE");

        var joinedGame = gameService.joinGame(joinRequest, authData.authToken());

        var storedGame = db.getGame(joinedGame.gameID());
        assertNotNull(joinedGame);
        assertNotNull(storedGame);
        assertEquals("joe", storedGame.whiteUsername());

    }

    @Test
    void joinGameSuccessBlack() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game = new GameData(0, null, null, "GhessGame", null, null);
        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        var gameData = gameService.createGame(game, authData.authToken());
        GameData joinRequest = new GameData(gameData.gameID(), null, null, gameData.gameName(), null, "BLACK");

        var joinedGame = gameService.joinGame(joinRequest, authData.authToken());

        var storedGame = db.getGame(joinedGame.gameID());
        assertNotNull(joinedGame);
        assertNotNull(storedGame);
        assertEquals("joe", storedGame.blackUsername());

    }

    @Test
    void joinGameUnauthorized() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game = new GameData(0, null, null, "GhessGame", null, null);
        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        var gameData = gameService.createGame(game, authData.authToken());
        userService.logout(authData.authToken());
        GameData joinRequest = new GameData(gameData.gameID(), null, null, gameData.gameName(), null, "BLACK");


        assertThrows(ErrorException.class, () -> gameService.joinGame(joinRequest, authData.authToken()));

    }

    @Test
    void joinGameTaken() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var secondUser = new UserData("bob", "notenoughsecrets", "b@b.com");
        var game = new GameData(0, null, null, "GhessGame", null, null);
        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        var secondAuthData = userService.register(secondUser);
        var gameData = gameService.createGame(game, authData.authToken());
        GameData joinRequest = new GameData(gameData.gameID(), null, null, gameData.gameName(), null, "BLACK");
        gameService.joinGame(joinRequest, authData.authToken());

        assertThrows(ErrorException.class, () -> gameService.joinGame(joinRequest, secondAuthData.authToken()));

    }


    @Test
    void listMultipleGames() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game1 = new GameData(0, null, null, "ChessGame", null, null);
        var game2 = new GameData(0, null, null, "ChessGame2", null, null);
        var game3 = new GameData(0, null, null, "ChessGame3", null, null);

        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        gameService.createGame(game1, authData.authToken());
        gameService.createGame(game2, authData.authToken());
        gameService.createGame(game3, authData.authToken());

        List<GameData> allGames = gameService.listGames(authData.authToken());

        assertNotNull(allGames);
        assertEquals(3, allGames.size());

    }

    @Test
    void listUnauthorized() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game1 = new GameData(0, null, null, "ChessGame", null, null);


        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        gameService.createGame(game1, authData.authToken());
        userService.logout(authData.authToken());

        assertThrows(ErrorException.class, () -> gameService.listGames(authData.authToken()));

    }

    @Test
    void clear() throws ErrorException, DataAccessException {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var game1 = new GameData(0, null, null, "ChessGame", null, null);
        var game2 = new GameData(0, null, null, "ChessGame2", null, null);
        var game3 = new GameData(0, null, null, "ChessGame3", null, null);

        var userService = new UserService(db);
        var gameService = new GameService(db);

        var authData = userService.register(user);
        gameService.createGame(game1, authData.authToken());
        gameService.createGame(game2, authData.authToken());
        gameService.createGame(game3, authData.authToken());

        gameService.clear();

        List<GameData> gamesAfter = db.getGames();

        assertTrue(gamesAfter.isEmpty());
    }
}