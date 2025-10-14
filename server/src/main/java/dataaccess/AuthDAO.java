package dataaccess;

import model.AuthData;

public interface AuthDAO {

    void createAuth(AuthData authToken) throws DataAccessException;

    AuthData getAuth(AuthData authToken) throws DataAccessException;

    void deleteAuth() throws DataAccessException;

    void clear() throws DataAccessException;
}
