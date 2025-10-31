package dataaccess;

import model.*;

import java.util.List;

public interface DataAccess {

    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    void clear() throws DataAccessException;
    void addAuth(AuthData authToken) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> getGames() throws DataAccessException;
    GameData updateGame(GameData gameID) throws DataAccessException;


}
