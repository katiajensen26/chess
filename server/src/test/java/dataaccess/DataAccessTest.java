package dataaccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @Test
    void getUser() {
    }

    @Test
    void createUser() {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void clear() {
        DataAccess db = new MemoryDataAccess();
        db.createUser(new UserData("joe", "j@J.com", "toomanysecrets"));
        db.clear();
        assertNull(db.getUser("joe"));
    }
}