package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    @Test
    void createGame() throws ErrorException {
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
    void createGameUnauthorized() throws ErrorException {
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
    void joinGameSuccessWhite() throws ErrorException {
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
    void joinGameSuccessBlack() throws ErrorException {
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
    void joinGameUnauthorized() throws ErrorException {
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
    void joinGameTaken() throws ErrorException {
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


}