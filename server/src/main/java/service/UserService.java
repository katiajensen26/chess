package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() {
        dataAccess.clear();
    }

    public AuthData register(UserData user) throws ErrorException{
        if (dataAccess.getUser(user.username()) != null) {
            throw new ErrorException("Error: Already Taken");
        }
        dataAccess.createUser(user);
        var authData = new AuthData(user.username(), generateToken());
        dataAccess.addAuth(authData);

        return authData;
    }

    public AuthData login(UserData user) throws ErrorException {

        UserData storedUser = dataAccess.getUser(user.username());

        if (storedUser == null) {
            throw new ErrorException("Error: username doesn't exist");
        }

        if (!storedUser.password().equals(user.password())) {
            throw new ErrorException("Error: unauthorized");
        }

        String authToken = generateToken();
        AuthData authData = new AuthData(user.username(), authToken);
        dataAccess.addAuth(authData);

        return authData;

    }

    public void logout(String authToken) throws ErrorException {
        AuthData storedAuth = dataAccess.getAuth(authToken);

        if (storedAuth == null) {
            throw new ErrorException("Error: unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
