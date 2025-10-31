package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws ErrorException, DataAccessException {
        try {
            dataAccess.clear();
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }

    public AuthData register(UserData user) throws ErrorException, DataAccessException {
        try {
            if (dataAccess.getUser(user.username()) != null) {
                throw new ErrorException("Error: Already Taken");
            }

            if (user.username() == null) {
                throw new ErrorException("Error: invalid username");
            }
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            UserData encryptedUser = new UserData(user.username(), hashedPassword, user.email());
            dataAccess.createUser(encryptedUser);
            var authData = new AuthData(user.username(), generateToken());
            dataAccess.addAuth(authData);

            return authData;
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }

    public AuthData login(UserData user) throws ErrorException, DataAccessException {
        try {
            UserData storedUser = dataAccess.getUser(user.username());

            if (storedUser == null) {
                throw new ErrorException("Error: username doesn't exist");
            }

            AuthData authData = null;
            if (BCrypt.checkpw(user.password(), storedUser.password())) {
                String authToken = generateToken();
                authData = new AuthData(user.username(), authToken);
                dataAccess.addAuth(authData);
            } else {
                throw new ErrorException("Error: unauthorized");

            }

            return authData;
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }

    public void logout(String authToken) throws ErrorException, DataAccessException {
        try {
            AuthData storedAuth = dataAccess.getAuth(authToken);

            if (storedAuth == null) {
                throw new ErrorException("Error: unauthorized");
            }

            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException("{\"message\": \"Error: failed to connect to database.\"}");
        }
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
