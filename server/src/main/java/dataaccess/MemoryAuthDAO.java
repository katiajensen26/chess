package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{

    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void addAuth(AuthData authToken) throws DataAccessException {
        authTokens.put(authToken.authToken(), authToken);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        authTokens.clear();
    }
}
