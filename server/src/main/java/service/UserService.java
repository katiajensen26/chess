package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws Exception{
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("already exists");
        }
        dataAccess.createUser(user);
        var authData = new AuthData(user.username(), generateAuthToken());

        return new AuthData(user.username(), generateAuthToken());
    }

    //replace this with the script from the specs
    private String generateAuthToken() {
        return "xyz";
    }
}
