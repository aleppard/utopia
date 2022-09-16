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
public class TraversalService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(TraversalService.class.getName());

    public Traversal getUserSeen(long userId,
                                 int startX,
                                 int startY,
                                 int width,
                                 int height) {
        Traversal traversal = new Traversal(startX, startY, width, height);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT x, y, has_seen FROM traversals WHERE user_id = ? AND x >= ? AND y >= ? AND x < ? AND y < ?");
            preparedStatement.setLong(1, userId);
            preparedStatement.setInt(2, startX);
            preparedStatement.setInt(3, startY);
            preparedStatement.setInt(4, startX + width);
            preparedStatement.setInt(5, startY + height);            
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                final boolean hasSeen = resultSet.getBoolean("has_seen");
                
                if (hasSeen) {
                    final int x = resultSet.getInt("x");
                    final int y = resultSet.getInt("y");

                    // Check returned result is within the bounds we requested.
                    Assert.assertTrue(x >= startX);
                    Assert.assertTrue(x < startX + width);
                    Assert.assertTrue(y >= startY);
                    Assert.assertTrue(y < startY + height);      
                    
                    traversal.hasSeen[y - startY][x - startX] = 1;
                }
            }
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            // MYTODO  - throws
            return null;
        }
        
        return traversal;
    }

    public static Integer[][] calculateSeen(int startX, int startY, int width, int height,
                                            int avatarX, int avatarY) {
        List<Integer[]> seen = new ArrayList<>();

        // @todo This could be made much faster!
        final int RADIUS = 6;

        final int centreX = avatarX;
        final int centreY = avatarY;
            
        for (int x = centreX - RADIUS; x <= centreX + RADIUS; x++) {
            for (int y = centreY - RADIUS; y <= centreY + RADIUS; y++) {
                if (x >= startX && y >= startY && x <width && y < height) {
                    if (Math.sqrt(Math.pow(centreX - x, 2) +
                                  Math.pow(centreY - y, 2)) < RADIUS) {
                        // @todo Use line of sight calculations.
                        // @todo Standard x,y ordering.
                        seen.add(new Integer[]{x, y});
                    }
                }
            }
        }

        // @todo This is cumbersome. Rethink how we handle these arrays.
        Integer[][] array = new Integer[seen.size()][2];
        for (int i = 0; i < seen.size(); i++) {
            array[i][0] = seen.get(i)[0];
            array[i][1] = seen.get(i)[1];
        }
        
        return array;
    }
    
    public void updateUserSeen(long userId, Integer[][] coordinates) {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO traversals (user_id, x, y, has_seen, has_visited) VALUES (?, ?, ?, ?, ?) ON CONFLICT (user_id, x, y) DO UPDATE SET has_seen = true");
            connection.setAutoCommit(false);            
            
            for (final Integer[] coordinate : coordinates) {
                // @todo Check each coordinate only has two values and they are within bounds.
                preparedStatement.setLong(1, userId);
                preparedStatement.setInt(2, coordinate[0]);
                preparedStatement.setInt(3, coordinate[1]);            
                preparedStatement.setBoolean(4, true);
                preparedStatement.setBoolean(5, false); // @todo Hook up
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();            
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }
    }
}
