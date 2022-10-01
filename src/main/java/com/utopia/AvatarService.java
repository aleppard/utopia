package com.utopia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access avatar images and avatar meta-data in the database.
 */
public class AvatarService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(AvatarService.class.getName());

    private List<Long> userAvatarIds;
    private Random random = new Random();
    
    public AvatarService() {
        findUserAvatarIds();
    }

    private void findUserAvatarIds() {
        userAvatarIds = new ArrayList();
    
        // Find the list of player avatar IDs.
        try (Connection connection = getConnection()) {        
            User user = new User();
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT id FROM avatars WHERE avatar_type='user'");
            preparedStatement.executeQuery();
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                final long id = resultSet.getInt("id");
                userAvatarIds.add(id);
            }
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            throw new RuntimeException(exception);            
        }
    }
    
    /**
     * Return an ID of a random avatar for a user.
     */
    public long getRandomUserAvatarId() {
        final int index = random.nextInt(userAvatarIds.size());
        return userAvatarIds.get(index);
    }
    
    public void add(final List<Avatar> avatars) {
        try (Connection connection = getConnection()) {                    
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("INSERT INTO avatars (description, avatar_type) VALUES (?, ?::avatar_type)");

            for (final Avatar avatar : avatars) {
                preparedStatement.setString(1, avatar.description);
                preparedStatement.setString(2, "user");
                preparedStatement.addBatch();
            }
                
            preparedStatement.executeBatch();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            throw new RuntimeException(exception);
        }
    }

    public byte[] getImage(long id) {
        try (Connection connection = getConnection()) {                    
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT image FROM avatars WHERE id=?");
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
            throw new RuntimeException(exception);            
        }

        throw new RuntimeException("Avatar image not found.");
    }

    public void setImage(long id, byte[] image) {
        try (Connection connection = getConnection()) {                    
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE avatars SET image=? WHERE id=?");
            preparedStatement.setBytes(1, image);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            throw new RuntimeException(exception);
        }            
    }
}
