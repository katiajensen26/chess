package dataaccess;

import model.GameData;

import javax.xml.crypto.Data;
import java.util.Collection;

public interface GameDAO {

    void createGame(GameData game) throws DataAccessException;

    GameData findGame(GameData gameID) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;

    void clear() throws DataAccessException;
}
