package dataaccess;

import model.*;

public interface DataAccess {

    UserData getUser(String username);
    void createUser(UserData user);
    void clear();
    void addAuth(AuthData authToken);
    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    void createGame(GameData gameName);
    GameData getGame(int gameID);
    GameData listGames(String authToken);
    GameData updateGame(GameData gameID);


}
