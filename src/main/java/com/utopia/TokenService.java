package com.utopia;

import java.security.SecureRandom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
public class TokenService extends Service
{
    private static final Logger LOGGER =
        Logger.getLogger(TokenService.class.getName());

    private static final int TOKEN_LENGTH = 64;
    
    public String generateTokenString() {
      StringBuffer token = new StringBuffer();
      SecureRandom random = new SecureRandom();
        
      for (int i = 0; i < TOKEN_LENGTH; i++) {
          int number = random.nextInt(16);
          token.append(String.format("%01X", number));
        }
        
        return token.toString();
    }
    
    public String createToken(long userId) {
        try {
            PreparedStatement preparedStatement =
                getConnection().prepareStatement
                ("INSERT INTO tokens (created_time, token, user_id) VALUES(?, ?, ?)");

            final OffsetDateTime now = OffsetDateTime.now();
            preparedStatement.setTimestamp
                (1,
                 Timestamp.valueOf(now.atZoneSameInstant(ZoneOffset.UTC)
                                   .toLocalDateTime()));

            // @todo We should store the token hashed (e.g. using bcrypt) as it's
            // essentially a password.
            final String tokenString = generateTokenString();
            preparedStatement.setString(2, tokenString);
            preparedStatement.setLong(3, userId);
            preparedStatement.executeUpdate();
            return tokenString;
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }

        return null;
    }
    
    public Token findToken(final String tokenString) {
        try {
            PreparedStatement preparedStatement =
                getConnection().prepareStatement
                ("SELECT id, created_time, user_id FROM tokens WHERE token=?");
            preparedStatement.setString(1, tokenString);
            preparedStatement.executeQuery();
            
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Token token = new Token();
                // @todo Set the other fields.
                token.userId = resultSet.getLong("user_id");
                return token;
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
