package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess{
    private final HashMap<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();


    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public void addAuth(AuthData authToken) {
        authTokens.put(authToken.authToken(), authToken);
    }

    @Override
    public AuthData getAuth(String authToken) {
         return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    @Override
    public void createGame(GameData gameName) {
        games.put(gameName.gameID(), gameName);
    }

    @Override
    public GameData getGame(GameData gameID) {
        return null;
    }

    @Override
    public GameData listGames(String authToken) {
        return null;
    }

    @Override
    public GameData updateGame(GameData gameID) {
        return null;
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
    }
}
