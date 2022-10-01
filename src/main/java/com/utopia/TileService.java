package com.utopia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access tile images and tile meta-data in the database.
 */
public class TileService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(TileService.class.getName());

    private TileConnectorService tileConnectorService =
        new TileConnectorService();

    /**
     * Return map that maps tile codes to their IDs in the database.
     * @todo Cache this?
     */
    public HashMap<String, Long> getCodeToIdMap() {
        try (Connection connection = getConnection()) {            
            HashMap<String, Long> map = new HashMap<>();
            
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT id, code FROM tiles");
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

    /**
     * Return tile ID for tile with code or null if not found.
     */
    public Long getTileId(final String code) {
        try (Connection connection = getConnection()) {            
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT id FROM tiles WHERE code=? LIMIT 1");
            preparedStatement.setString(1, code);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }

        return null;
    }
    
    public HashMap<Long, Boolean> getTraverseability() {
        try (Connection connection = getConnection()) {            
            // @todo Using an array would be better here.
            HashMap<Long, Boolean> traverseability = new HashMap<>();
            
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT id, is_traverseable FROM tiles");
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                final long id = resultSet.getLong("id");
                final boolean isTraverseable =
                    resultSet.getBoolean("is_traverseable");
                traverseability.put(id, isTraverseable);
            }
            
            return traverseability;
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            return null;
        }
    }
    
    public void add(final List<Tile> tiles) {
        final HashMap<String, Long> tileConnectorCodeToIdMap =
            tileConnectorService.getCodeToIdMap();
        
        try (Connection connection = getConnection()) {                    
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("INSERT INTO tiles (tile_type, is_traverseable, north_connector_id, east_connector_id, south_connector_id, west_connector_id, code, description) VALUES (?::tile_type, ?, ?, ?, ?, ?, ?, ?)");

            for (final Tile tile : tiles) {
                preparedStatement.setString(1, "land");
                preparedStatement.setBoolean(2, tile.isTraverseable);
                preparedStatement.setObject
                    (3, tileConnectorCodeToIdMap.get(tile.northConnectorCode), Types.BIGINT);
                preparedStatement.setObject
                    (4, tileConnectorCodeToIdMap.get(tile.eastConnectorCode), Types.BIGINT);
                preparedStatement.setObject
                    (5 ,tileConnectorCodeToIdMap.get(tile.southConnectorCode), Types.BIGINT);
                preparedStatement.setObject
                    (6, tileConnectorCodeToIdMap.get(tile.westConnectorCode), Types.BIGINT);
                preparedStatement.setString(7, tile.code);
                preparedStatement.setString(8, tile.description);

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

    public byte[] getImage(long id) {
        try (Connection connection = getConnection()) {                    
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT image FROM tiles WHERE id=?");
            preparedStatement.setLong(1, id);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBytes("image");
            }
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }

        // @todo
        return null;
    }

    public void setImage(long id, byte[] image) {
        try (Connection connection = getConnection()) {                    
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tiles SET image=? WHERE id=?");
            preparedStatement.setBytes(1, image);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }            
    }
}
