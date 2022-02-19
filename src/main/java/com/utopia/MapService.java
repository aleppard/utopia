package com.utopia;

import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MapService implements AutoCloseable
{
    private static final Logger LOGGER =
        Logger.getLogger(MapService.class.getName());
    
    private Connection connection;

    private int minX = 0;
    private int minY = 0;
    private int maxX = 0;
    private int maxY = 0;
    
    public MapService() {
        try (InputStream input =
             MapService.class.getClassLoader().getResourceAsStream("config.properties")) {            
            Properties properties = new Properties();
            properties.load(input);

            // Load PostgreSQL driver.
            Class.forName("org.postgresql.Driver");
            
            connection = DriverManager.getConnection
                ("jdbc:postgresql://" +
                 properties.getProperty("database.host") + ":" +
                 properties.getProperty("database.port") + "/utopia",
                 properties.getProperty("database.user"),
                 properties.getProperty("database.password"));
            if (connection == null) throw new Exception();
            
            findMinMax();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }
        catch (Exception exception) {
            LOGGER.log(Level.SEVERE,
                       "Error connection to PostgreSQL.",
                       exception);
        }
    }

    private void findMinMax() throws SQLException {
        PreparedStatement preparedStatement =
            connection.prepareStatement("SELECT MIN(x), MAX(x), MIN(y), MAX(y) FROM game_map");
        ResultSet resultSet = preparedStatement.executeQuery();
        
        resultSet.next();
        minX = resultSet.getInt(0);
        maxX = resultSet.getInt(1);
        minY = resultSet.getInt(2);
        maxY = resultSet.getInt(3);                                    
    }
    
    public int getWidth() {
        // Include the 0 index in the width.
        // @todo This assumes minX < 0 and maxX > 0.
        return Math.abs(minX) + maxX + 1;
    }

    public int getHeight() {
        // Include the 0 index in the height.
        // @todo This assumes minY < 0 and maxY > 0.        
        return Math.abs(minY) + maxY + 1;
    }
    
    public Map getMap() {
        try {
            Map map = new Map(minX,
                              minY,
                              getWidth(),
                              getHeight());
            PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM game_map ORDER BY y, x");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");

                List<Integer> tiles = new ArrayList();
                tiles.add(resultSet.getInt("base_tile_id"));

                {
                    int overlayTile1Id = resultSet.getInt("overlay_tile1_id");
                    if (!resultSet.wasNull()) tiles.add(overlayTile1Id);
                }

                {
                    int overlayTile2Id = resultSet.getInt("overlay_tile2_id");
                    if (!resultSet.wasNull()) tiles.add(overlayTile2Id);
                }

                {
                    int overlayTile3Id = resultSet.getInt("overlay_tile3_id");
                    if (!resultSet.wasNull()) tiles.add(overlayTile3Id);
                }
                
                map.tiles[y - minY][x - minX] = tiles;
            }
            
            return map;
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            // MYTODO  - throws
            return null;
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) connection.close();
    }
}
