package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws AlreadyTakenException {
        if (dataAccess.getUser(user.username()) != null) {
            throw new AlreadyTakenException("Error: Already Taken");
        }
        dataAccess.createUser(user);
        var authData = new AuthData(user.username(), generateAuthToken());

        return new AuthData(user.username(), generateAuthToken());
    }

    public static class AlreadyTakenException extends Exception {
        private final String message;

        public AlreadyTakenException(String message) {
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
