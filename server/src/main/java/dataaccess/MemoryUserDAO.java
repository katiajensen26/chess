package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {

    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {

        if (users.get(username) == null) {
            throw new DataAccessException("User doesn't exist.");
        } else {
            return users.get(username);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }
}
