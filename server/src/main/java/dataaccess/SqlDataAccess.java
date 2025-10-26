package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public class SqlDataAccess implements DataAccess {
    public SqlDataAccess() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex){
            throw new RuntimeException("Error: something is wrong", ex);
        }
    }

    //select from user table
    @Override
    public UserData getUser(String username) {
        return null;
    }

    //insert into user table
    @Override
    public void createUser(UserData user) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void addAuth(AuthData authToken) {

    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) {

    }

    @Override
    public void createGame(GameData gameName) {

    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public List<GameData> getGames() {
        return List.of();
    }

    @Override
    public GameData updateGame(GameData gameID) {
        return null;
    }
}
