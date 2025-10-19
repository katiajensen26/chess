package dataaccess;

import model.*;

public interface DataAccess {

    UserData getUser(String username);
    void createUser(UserData user);
    void clear();
    void addAuth(AuthData authToken);
    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);

}
