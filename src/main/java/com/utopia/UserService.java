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
        final int x = -3;
        final int y = -3;
        final String name = nameGenerator.generateName();

        try {
            PreparedStatement preparedStatement =
                getConnection().prepareStatement
                ("INSERT INTO users (created_time, last_seen_time, last_x, last_y, avatar_name) VALUES (?, ?, ?, ?, ?)",
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
            preparedStatement.setString(5, name);
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
        try {
            User user = new User();
            PreparedStatement preparedStatement =
                getConnection().prepareStatement
                ("SELECT created_time, last_seen_time, last_x, last_y, avatar_name FROM users WHERE id=?");
            preparedStatement.setLong(1, userId);
            preparedStatement.executeQuery();
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                // @todo Set created and last seen times.
                user.lastX = resultSet.getInt("last_x");
                user.lastY = resultSet.getInt("last_y");
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
}
