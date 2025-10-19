package dataaccess;

import model.AuthData;

public interface AuthDAO {

    void addAuth(AuthData authToken) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    void clear() throws DataAccessException;
}
