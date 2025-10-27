package dataaccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

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
        assertNull(db.getUser("joe"));
    }

    @Test
    void addAuth() {
    }

    @Test
    void getAuth() {
    }

    @Test
    void deleteAuth() {
    }

    @Test
    void createGame() {
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