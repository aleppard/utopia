package com.utopia;

import java.security.SecureRandom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class UserService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(UserService.class.getName());

    private NameGenerator nameGenerator = new NameGenerator();
    
    public User createNewUser() {
        final OffsetDateTime now = OffsetDateTime.now();
        final int x = 5;
        final int y = 5;
        final String direction = "east";
        final String name = nameGenerator.generateName();

        try (Connection connection = getConnection()) {        
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("INSERT INTO users (created_time, last_seen_time, last_x, last_y, last_direction, avatar_name) VALUES (?, ?, ?, ?, ?::direction_type, ?)",
                 Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setTimestamp
                (1,
                 Timestamp.valueOf(now.atZoneSameInstant(ZoneOffset.UTC)
                                   .toLocalDateTime()));
            preparedStatement.setTimestamp
                (2,
                 Timestamp.valueOf(now.atZoneSameInstant(ZoneOffset.UTC)
                                   .toLocalDateTime()));                 
            preparedStatement.setInt(3, x);
            preparedStatement.setInt(4, y);
            preparedStatement.setString(5, direction);
            preparedStatement.setString(6, name);
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            
            User user = new User();
            user.id = generatedKeys.getLong(1);
            user.createdTime = now;
            user.lastSeenTime = now;
            user.lastX = x;
            user.lastY = y;
            user.name = name;
            return user;
         }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            return null;
        }            
    }

    public User getUser(long userId) {
        try (Connection connection = getConnection()) {        
            User user = new User();
            PreparedStatement preparedStatement =
                connection.prepareStatement
                ("SELECT created_time, last_seen_time, last_x, last_y, last_direction, avatar_name FROM users WHERE id=?");
            preparedStatement.setLong(1, userId);
            preparedStatement.executeQuery();
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                user.id = userId;
                user.lastX = resultSet.getInt("last_x");
                user.lastY = resultSet.getInt("last_y");
                user.lastDirection = resultSet.getString("last_direction");
                user.name = resultSet.getString("avatar_name");
                return user;
            }
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }
        return null;
    }

    public void updateUserLastLocation(long userId, int x, int y, final String direction) {
        try (Connection connection = getConnection()) {        
            // @todo Also update user's last seen time.
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET last_x=?, last_y=?, last_direction=?::direction_type WHERE id=?");
            preparedStatement.setInt(1, x);
            preparedStatement.setInt(2, y);
            preparedStatement.setString(3, direction);
            preparedStatement.setLong(4, userId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }        
    }
}
