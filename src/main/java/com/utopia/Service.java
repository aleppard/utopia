package com.utopia;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Service
{
    private static final Logger LOGGER =
        Logger.getLogger(Service.class.getName());
    
    public Service() {}

    protected Connection getConnection() {
        try {
            return ConnectionPool.getConnection();
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
            return null;
        }
    }
}
