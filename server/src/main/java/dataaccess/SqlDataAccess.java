package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlDataAccess implements DataAccess {
    public SqlDataAccess() {
        try {
            DatabaseManager.createDatabase();
            createTables();
        } catch (DataAccessException ex){
            throw new RuntimeException("Error: database or tables were not made", ex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT username, password, email FROM users WHERE username=?")) {
                preparedStatement.setString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: Can't connect to database", e);
        }
        return null;
    }

    //insert into user table
    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, user.username());
                preparedStatement.setString(2, user.password());
                preparedStatement.setString(3, user.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Can't insert user into database.");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.createStatement()) {
                preparedStatement.executeUpdate("DELETE FROM auth");
                preparedStatement.executeUpdate("DELETE FROM users");
                preparedStatement.executeUpdate("DELETE FROM games");
                //preparedStatement.executeUpdate("ALTER TABLE games AUTO_INCREMENT = 1");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: Couldn't connect to the database.");
        }
    }

    @Override
    public void addAuth(AuthData authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO auth (authToken, username) VALUES (?, ?)")) {
                preparedStatement.setString(1, authToken.authToken());
                preparedStatement.setString(2, authToken.username());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Can't insert auth into database.");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT authToken, username FROM auth WHERE authToken=?")) {
                preparedStatement.setString(1, authToken);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("username"), rs.getString("authToken"));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: couldn't connect to the database.");
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM auth WHERE authToken=?")) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: couldn't connect to the database.");
        }
    }

    @Override
    public int createGame(GameData gameName) throws DataAccessException {
        String serializedGame = new Gson().toJson(gameName.game());
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, gameName.whiteUsername());
                preparedStatement.setString(2, gameName.blackUsername());
                preparedStatement.setString(3, gameName.gameName());
                preparedStatement.setString(4, serializedGame);
                preparedStatement.executeUpdate();

                try (var rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: couldn't connect to the database.");
        }
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID=?")) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        ChessGame deserializedGame = new Gson().fromJson(rs.getString("game"), ChessGame.class);
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                deserializedGame,
                                null);

                    } else {
                        throw new DataAccessException("Error: Game not found");
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: couldn't connect to the database.");
        }
    }

    @Override
    public List<GameData> getGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            try (var prepareStatement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games")) {
                try (var rs = prepareStatement.executeQuery()) {
                    while (rs.next()) {
                        ChessGame deserializedGame = new Gson().fromJson(rs.getString("game"), ChessGame.class);
                        GameData item = new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                deserializedGame,
                                null);
                        result.add(item);
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: couldn't connect to the database.");
        }
        return result;
    }

    @Override
    public GameData updateGame(GameData gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("UPDATE games SET whiteUsername=?, blackUsername=? WHERE gameID=?")) {
                preparedStatement.setString(1, gameID.whiteUsername());
                preparedStatement.setString(2, gameID.blackUsername());
                preparedStatement.setInt(3, gameID.gameID());
                preparedStatement.executeUpdate();
                GameData updatedGame = getGame(gameID.gameID());
                if (updatedGame != null) {
                    return updatedGame;
                } else {
                    return gameID;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: couldn't connect to the database.");
        }
    }

    private void createTables() throws SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.createStatement()) {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS users (
                            username VARCHAR(255) PRIMARY KEY,
                            password VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL
                        )
                """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS auth (
                            authToken VARCHAR(255) PRIMARY KEY,
                            username VARCHAR(255) NOT NULL,
                            FOREIGN KEY (username) REFERENCES users(username)
                        )
               """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS games(
                            gameID INT AUTO_INCREMENT PRIMARY KEY,
                            whiteUsername VARCHAR(255),
                            blackUsername VARCHAR(255),
                            gameName VARCHAR(255) NOT NULL,
                            game LONGTEXT NOT NULL
                        )
                """);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Error: tables can't be created", e);
        }
    }
}
