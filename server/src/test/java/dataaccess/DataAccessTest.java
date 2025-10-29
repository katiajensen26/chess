package dataaccess;

import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @BeforeEach
    void setUp() {
        DataAccess db = new SqlDataAccess();
        db.clear();
    }

    @Test
    void getUser() {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@J.com");
        db.createUser(user);
        UserData storedUser = db.getUser(user.username());
        assertEquals(user, storedUser);
    }

    @Test
    void createUser() {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@J.com");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void clear() {
        DataAccess db = new SqlDataAccess();
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.clear();
        UserData user = db.getUser("joe");
        assertNull(user);
    }

    @Test
    void addAuth() {
        DataAccess db = new SqlDataAccess();
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(new AuthData("joe", "token123"));

        AuthData auth = db.getAuth("token123");
        assertNotNull(auth);
        assertEquals("joe", auth.username());
        assertEquals("token123", auth.authToken());
    }

    @Test
    void getAuth() {
        DataAccess db = new SqlDataAccess();
        var auth = new AuthData("joe", "token123");
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(auth);

        AuthData storedAuth = db.getAuth("token123");
        assertEquals(auth, storedAuth);
    }

    @Test
    void deleteAuth() {
        DataAccess db = new SqlDataAccess();
        var auth = new AuthData("joe", "token123");
        db.createUser(new UserData("joe", "toomanysecrets", "j@J.com"));
        db.addAuth(auth);

        db.deleteAuth(auth.authToken());

        assertNull(db.getAuth(auth.authToken()));
    }

    @Test
    void createGame() {
        DataAccess db = new SqlDataAccess();

    }

    @Test
    void getGame() {
    }

    @Test
    void getGames() {
    }

    @Test
    void updateGame() {
    }
}