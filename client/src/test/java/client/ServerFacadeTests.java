package client;

import model.UserData;
import org.junit.jupiter.api.*;
import server.ResponseException;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
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
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.register(newUser));
        assertEquals(ResponseException.StatusCode.AlreadyTaken, ex.code());
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
