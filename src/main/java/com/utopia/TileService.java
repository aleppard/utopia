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
 *
 */
public class TileService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(TileService.class.getName());

    public HashMap<Integer, Boolean> getTraverseability() {
        try {
            // @todo Using an array would be better here.
            HashMap<Integer, Boolean> traverseability = new HashMap<>();
            
            PreparedStatement preparedStatement =
                getConnection().prepareStatement
                ("SELECT id, is_traverseable FROM tiles");
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                boolean isTraverseable = resultSet.getBoolean("is_traverseable");
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
    
    public void add(Tile tile) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO tiles (is_traverseable, code, description, tile_type) VALUES (?, ?, ?, ?::tile_enum)");
            preparedStatement.setBoolean(1, tile.isTraverseable);
            preparedStatement.setString(2, tile.code);
            preparedStatement.setString(3, tile.description);
            preparedStatement.setString(4, "land");
            preparedStatement.executeUpdate();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }
    }

    public byte[] getImage(long id) {
        try {
            PreparedStatement preparedStatement =
                getConnection().prepareStatement
                ("SELECT image FROM tiles WHERE id=?");
            preparedStatement.setLong(1, id);
            preparedStatement.executeQuery();
            
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
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE tiles SET image=? WHERE id=?");
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
