package com.utopia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access to tile connectios stored in the database. A tile connection
 * defines which adjacent tiles can be neighbours.
 */
public class TileConnectorService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(TileConnectorService.class.getName());

    /**
     * Return map that maps tile connector codes to their IDs in the database.
     * @todo Cache this?
     */
    public HashMap<String, Long> getCodeToIdMap() {
        try (Connection connection = getConnection()) {            
            HashMap<String, Long> map = new HashMap<>();
            
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT id, code FROM tile_connectors");
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                final long id = resultSet.getLong("id");
                final String code = resultSet.getString("code");
                map.put(code, id);
            }
            
            return map;
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            return null;
        }
    }
    
    public void add(final List<TileConnector> tileConnectors) {
        try (Connection connection = getConnection()) {                    
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("INSERT INTO tile_connectors (code, description) VALUES (?, ?)");

            for (final TileConnector tileConnector : tileConnectors) {
                preparedStatement.setString(1, tileConnector.code);
                preparedStatement.setString(2, tileConnector.description);
                preparedStatement.addBatch();
            }
            
            preparedStatement.executeBatch();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }
    }
}
