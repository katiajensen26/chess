package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @BeforeEach
    void setUp() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        db.clear();
    }

    @Test
    void getUser() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@J.com");
        db.createUser(user);
        UserData storedUser = db.getUser(user.username());
        assertEquals(user, storedUser);
    }

    @Test
    void getUserFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@J.com");
        db.createUser(user);

        UserData result = db.getUser("bob");

        assertNull(result);
    }

    @Test
    void createUser() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@J.com");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void createUserFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var user1 = new UserData("joe", "toomanysecrets", "j@J.com");
        var user2 = new UserData("joe", "notenough", "b@B.com");
        db.createUser(user1);

        assertThrows(DataAccessException.class, () -> db.createUser(user2));
    }

    @Test
    void clear() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.clear();
        UserData user = db.getUser("joe");
        assertNull(user);
    }

    @Test
    void addAuth() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(new AuthData("joe", "token123"));

        AuthData auth = db.getAuth("token123");
        assertNotNull(auth);
        assertEquals("joe", auth.username());
        assertEquals("token123", auth.authToken());
    }

    @Test
    void addAuthFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));

        assertThrows(DataAccessException.class, () -> db.addAuth(new AuthData("bob", "token123")));
    }

    @Test
    void getAuth() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var auth = new AuthData("joe", "token123");
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(auth);

        AuthData storedAuth = db.getAuth("token123");
        assertEquals(auth, storedAuth);
    }

    @Test
    void getAuthFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var auth = new AuthData("joe", "token123");
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(auth);

        AuthData storedAuth = db.getAuth("token456");
        assertNull(storedAuth);
    }

    @Test
    void deleteAuth() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var auth = new AuthData("joe", "token123");
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(auth);

        db.deleteAuth(auth.authToken());

        assertNull(db.getAuth(auth.authToken()));
    }

    @Test
    void deleteAuthFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var auth = new AuthData("joe", "token123");
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(auth);

        db.deleteAuth("token234");

        assertNotNull(db.getAuth(auth.authToken()));
    }

    @Test
    void createGame() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var game1 = new GameData(0, null, null, "game1", new ChessGame(), null);
        var storedGameID = db.createGame(game1);

        var dbGame = db.getGame(storedGameID);

        assertEquals(storedGameID, dbGame.gameID());

    }

    @Test
    void createGameFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var game = new GameData(0, null, null, null, new ChessGame(), null);

        assertThrows(DataAccessException.class, () -> db.createGame(game));

    }

    @Test
    void getGame() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var game = new GameData(0, null, null, "game2", new ChessGame(), null);
        var storedGameID = db.createGame(game);

        var storedGame = db.getGame(storedGameID);

        assertEquals(storedGameID, storedGame.gameID());
    }

    @Test
    void getGameFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var game = new GameData(0, null, null, "game2", new ChessGame(), null);
        db.createGame(game);

        assertThrows(DataAccessException.class, () -> db.getGame(3));
    }

    @Test
    void getGames() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var game1 = new GameData(0, null, null, "game1", new ChessGame(), null);
        var game2 = new GameData(0, null, null, "game2", new ChessGame(), null);
        var game3 = new GameData(0, null, null, "game3", new ChessGame(), null);
        db.createGame(game1);
        db.createGame(game2);
        db.createGame(game3);

        List<GameData> allGames = db.getGames();

        assertNotNull(allGames);
        assertEquals(3, allGames.size());

    }

    @Test
    void getGamesNoGames() throws DataAccessException {
        DataAccess db = new SqlDataAccess();

        List<GameData> allGames = db.getGames();

        assertTrue(allGames.isEmpty());

    }

    @Test
    void updateGame() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var game = new GameData(0, null, null, "game1", new ChessGame(), null);
        var storedGame = db.createGame(game);

        var GametoUpdate = new GameData(storedGame, "myUsername", null, "game1", new ChessGame(), null);

        var updatedGame = db.updateGame(GametoUpdate);

        assertNotNull(updatedGame);
        assertEquals(GametoUpdate.whiteUsername(), updatedGame.whiteUsername());

    }

    @Test
    void updateGameFailure() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        var gameToUpdate = new GameData(3, "myUsername", null, "game1", new ChessGame(), null);
        
        assertThrows(DataAccessException.class, () -> db.updateGame(gameToUpdate));

    }
}