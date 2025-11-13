package client;

import dataaccess.DataAccessException;
import dataaccess.SqlDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import ui.ResponseException;
import server.Server;
import ui.ServerFacade;
import service.ErrorException;
import service.UserService;
import service.GameService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static UserService userService;
    private static GameService gameService;
    private static SqlDataAccess db;

    @BeforeAll
    public static void init() {
        server = new Server();
        db = new SqlDataAccess();
        userService = new UserService(db);
        gameService = new GameService(db);
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void setUp() throws ErrorException, DataAccessException {
        userService.clear();
        gameService.clear();
    }

    @Test
    public void registerSuccess() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        var authData = facade.register(newUser);
        assertNotNull(authData);
        assertEquals(authData.username(), newUser.username());
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    public void registerTwice() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        facade.register(newUser);
        assertThrows(ResponseException.class, () -> facade.register(newUser));
    }

    @Test
    public void loginSuccess() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        facade.register(newUser);
        var authData = facade.login(newUser);
        assertNotNull(authData);
        assertEquals(authData.username(), newUser.username());
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    public void loginWrongPassword() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        facade.register(newUser);
        UserData loginAttempt = new UserData("player1", "wrongpassword", "player1@email.com");
        assertThrows(ResponseException.class, () -> facade.login(loginAttempt));
    }

    @Test
    public void logoutSuccess() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        facade.register(newUser);
        var authData = facade.login(newUser);

        assertDoesNotThrow(() -> facade.logout(authData));
    }

    @Test
    public void logoutFailure() {
        AuthData fakeAuth = new AuthData("player1", "authToken");

        assertThrows(ResponseException.class, () -> facade.logout(fakeAuth));
    }

    @Test
    public void createGameSuccess() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        var authData = facade.register(newUser);

        var newGame = facade.createGame(authData, "game1");

        assertNotNull(newGame.gameID());
        assertNotNull(newGame);
        assertEquals("game1", newGame.gameName());
    }

    @Test
    public void createGameFailure() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        var authData = facade.register(newUser);

        assertNotNull(authData);
        assertThrows(ResponseException.class, () -> facade.createGame(authData, ""));
    }

    @Test
    public void listGamesSuccess() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        var authData = facade.register(newUser);

        facade.createGame(authData, "game1");
        facade.createGame(authData, "game2");

        Map<String, List<GameData>> gamesList = facade.listGames(authData);

        List<GameData> gamesArray = gamesList.get("games");

        assertNotNull(gamesList);
        assertEquals(2, gamesArray.size());
    }

    @Test
    public void listGamesFailure() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        var authData = facade.register(newUser);

        facade.createGame(authData, "game1");
        facade.createGame(authData, "game2");

        AuthData fakeAuth = new AuthData("player1", "authToken");

        assertThrows(ResponseException.class, () -> facade.listGames(fakeAuth));
    }

    @Test
    public void joinGameSuccess() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        var authData = facade.register(newUser);

        var newGame = facade.createGame(authData, "game1");

        assertDoesNotThrow(() -> facade.joinGame(authData, "WHITE", newGame.gameID()));
    }

    @Test
    public void joinGameFailure() {
        UserData newUser = new UserData("player1", "player1password", "player1@email.com");
        var authData = facade.register(newUser);

        var newGame = facade.createGame(authData, "game1");

        assertThrows(ResponseException.class, () -> facade.joinGame(authData, "BLUE", newGame.gameID()));

    }


    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

}
