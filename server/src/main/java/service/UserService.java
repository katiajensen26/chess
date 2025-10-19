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

    public AuthData register(UserData user) throws ErrorException {
        if (dataAccess.getUser(user.username()) != null) {
            throw new ErrorException("Error: Already Taken");
        }
        dataAccess.createUser(user);
        var authData = new AuthData(user.username(), generateToken());

        return new AuthData(user.username(), generateToken());
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

    //fix this
    public void logout(String authToken) {

        AuthData storedAuth = dataAccess.getAuth(authToken);

        dataAccess.deleteAuth(authToken);
    }

    public static class ErrorException extends Exception {
        private final String message;

        public ErrorException(String message) {
            super(message);
            this.message = message;
        }

        public String getMessage () {
            return String.format("{\"message\": \"%s\"}", message);
        }

    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
