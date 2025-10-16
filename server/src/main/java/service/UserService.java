package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws ErrorException {
        if (dataAccess.getUser(user.username()) != null) {
            throw new ErrorException("Error: Already Taken");
        }
        dataAccess.createUser(user);
        var authData = new AuthData(user.username(), generateAuthToken());

        return new AuthData(user.username(), generateAuthToken());
    }

    public AuthData login(UserData user) throws ErrorException {

        UserData storedUser = dataAccess.getUser(user.username());

        if (storedUser == null) {
            throw new ErrorException("Error: username doesn't exist");
        }

        if (!storedUser.password().equals(user.password())) {
            throw new ErrorException("Error: unauthorized");
        }


        return new AuthData(user.username(), generateAuthToken());

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

    //replace this with the script from the specs
    private String generateAuthToken() {
        return "xyz";
    }
}
