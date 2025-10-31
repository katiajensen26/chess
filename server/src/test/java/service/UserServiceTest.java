package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SqlDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @BeforeEach
    void setup() throws DataAccessException {
        DataAccess db = new SqlDataAccess();
        db.clear();
        UserService userService = new UserService(db);
    }

    @Test
    void register() throws Exception {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "j@J.com", "toomanysecrets");
        var userService = new UserService(db);

        var authData = userService.register(user);

        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertFalse(authData.authToken().isEmpty());

    }

    @Test
    void registerInvalidUsername() throws Exception {
        DataAccess db = new SqlDataAccess();
        var user = new UserData(null, "j@J.com", "toomanysecrets");
        var userService = new UserService(db);

        assertThrows(ErrorException.class, () -> userService.register(user));

    }

    @Test
    void clear() {
    }

    @Test
    void login() throws Exception {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@j.com");
        var userService = new UserService(db);

        userService.register(user);
        var authData = userService.login(user);

        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertFalse(user.username().isEmpty());
    }

    @Test
    void loginWrongUsername() throws Exception {
        DataAccess db = new SqlDataAccess();
        var firstUser = new UserData("joe", "toomanysecrets", "j@j.com");
        var secondUser = new UserData("bob", "notenoughsecrets", "b@b.com");
        var userService = new UserService(db);

        userService.register(firstUser);

        assertThrows(ErrorException.class, () -> userService.login(secondUser));
    }

    @Test
    void loginWrongPassword() throws Exception {
        DataAccess db = new SqlDataAccess();
        var firstAttempt = new UserData("joe", "toomanysecrets", "j@j.com");
        var secondAttempt = new UserData("joe", "notenoughsecrets", "j@j.com");
        var userService = new UserService(db);

        userService.register(firstAttempt);

        assertThrows(ErrorException.class, () -> userService.login(secondAttempt));
    }

    @Test
    void logoutSuccess() throws Exception {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@j.com");
        var userService = new UserService(db);

        var authData = userService.register(user);
        userService.logout(authData.authToken());

        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    void logoutTwice() throws Exception {
        DataAccess db = new SqlDataAccess();
        var user = new UserData("joe", "toomanysecrets", "j@j.com");
        var userService = new UserService(db);

        var authData = userService.register(user);
        userService.logout(authData.authToken());

        assertThrows(ErrorException.class, () -> userService.logout(authData.authToken()));
    }
}