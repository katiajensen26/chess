package dataaccess;

import model.*;

import java.util.List;

public interface DataAccess {

    UserData getUser(String username);
    void createUser(UserData user);
    void clear();
    void addAuth(AuthData authToken);
    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    int createGame(GameData gameName);
    GameData getGame(int gameID);
    List<GameData> getGames();
    GameData updateGame(GameData gameID);


}
