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
 *
 */
public class MapService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(MapService.class.getName());
    
    private int minX = 0;
    private int minY = 0;
    private int maxX = 0;
    private int maxY = 0;

    private TileService tileService = new TileService();
    
    public MapService() {
        findMinMax();
    }

    private void findMinMax() {
        try (Connection connection = getConnection()) {        
            PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT MIN(x), MAX(x), MIN(y), MAX(y) FROM game_map");
            ResultSet resultSet = preparedStatement.executeQuery();
            
            resultSet.next();
            minX = resultSet.getInt(1);
            maxX = resultSet.getInt(2);
            minY = resultSet.getInt(3);
            maxY = resultSet.getInt(4);
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            // @todo
        }
    }

    public int getStartX() {
        return minX;
    }

    public int getStartY() {
        return minY;
    }
    
    public int getWidth() {
        // Adding one as the min and max are both inclusive.
        return (maxX - minX + 1);
    }

    public int getHeight() {
        // See getWidth().
        return (maxY - minY + 1);
    }

    public Region getBounds() {
        return new Region(getStartX(), getStartY(), getWidth(), getHeight());
    }
    
    public Map getMap() {
        try (Connection connection = getConnection()) {        
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

                // @todo Check the x, y is within bounds. If not call findMinMax()
                // again.
                map.tiles[y - minY][x - minX] = tiles;

                final boolean isTraverseable =
                    resultSet.getBoolean("is_traverseable");
                if (isTraverseable) {
                    map.isTraverseable[y - minY][x - minX] = 1;
                }
            }
            
            return map;
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            // @todo
            return null;
        }
    }

    // @todo Pass Region.
    public Map getMap(int startX, int startY, int width, int height) {
        try (Connection connection = getConnection()) {
            // @todo Roll this protection out everywhere and move to
            // the Region class.
            startX = Math.max(startX, minY);
            startY = Math.max(startY, minY);
            width = Math.min(width, maxX - startX + 1);
            height = Math.min(height, maxY - startY + 1);
            
            Map map = new Map(startX,
                              startY,
                              width,
                              height);
            PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM game_map WHERE x >= ? AND y >= ? AND x < ? AND y < ?  ORDER BY y, x ");
            preparedStatement.setInt(1, startX);
            preparedStatement.setInt(2, startY);
            preparedStatement.setInt(3, startX + width);
            preparedStatement.setInt(4, startY + height);
            
            ResultSet resultSet = preparedStatement.executeQuery();

            // @todo Refactor
            while (resultSet.next()) {
                final int x = resultSet.getInt("x");
                final int y = resultSet.getInt("y");

                // Check returned result is within the bounds we requested.
                Assert.assertTrue(x >= startX);
                Assert.assertTrue(x < startX + width);
                Assert.assertTrue(y >= startY);
                Assert.assertTrue(y < startY + height);                
                
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

                map.tiles[y - startY][x - startX] = tiles;

                final boolean isTraverseable =
                    resultSet.getBoolean("is_traverseable");
                if (isTraverseable) {
                    map.isTraverseable[y - startY][x - startX] = 1;
                }
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

    private static boolean isTraverseable
        (final HashMap<Integer, Boolean> traverseability,
         final List<Integer> tileIds) {
        for (final Integer tileId : tileIds) {
            if (!traverseability.get(tileId)) return false;
        }
            
        return true;
    }
    
    public void setMap(InputMap map) {
        HashMap<Integer, Boolean> traverseability =
            tileService.getTraverseability();

        // @todo Probably faster to store each row separately.
        try (Connection connection = getConnection()) {        
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_map (x, y, base_tile_id, overlay_tile1_id, overlay_tile2_id, overlay_tile3_id, is_traverseable) VALUES (?, ?, ?, ?, ?, ?, ?)");           
            for (int i = 0; i < map.height; i++) {
                int y = map.startY + i;
                
                for (int j = 0; j < map.width; j++) {
                    int x = map.startX + j;

                    // @todo tiles -> tileIds.
                    List<Integer> tiles = map.tiles[i][j];
                    preparedStatement.setInt(1, x);
                    preparedStatement.setInt(2, y);

                    preparedStatement.setInt(3, tiles.get(0));
                    if (tiles.size() > 1) {
                        preparedStatement.setInt(4, tiles.get(1));
                    }
                    else {
                        preparedStatement.setNull(4, Types.INTEGER);
                    }

                    if (tiles.size() > 2) {
                        preparedStatement.setInt(5, tiles.get(2));
                    }
                    else {
                        preparedStatement.setNull(5, Types.INTEGER);
                    }

                    if (tiles.size() > 3) {
                        preparedStatement.setInt(6, tiles.get(3));
                    }
                    else {
                        preparedStatement.setNull(6, Types.INTEGER);
                    }                    

                    preparedStatement.setBoolean
                        (7, isTraverseable(traverseability, tiles));
                    
                    preparedStatement.executeUpdate();
                }
            }
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            // MYTODO  - throws
        }
    }
}
