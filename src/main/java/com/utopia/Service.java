package com.utopia;

import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Service implements AutoCloseable
{
    private static final Logger LOGGER =
        Logger.getLogger(Service.class.getName());
    
    private Connection connection;

    private void open() {
        try (InputStream input =
             MapService.class.getClassLoader().getResourceAsStream("properties/database.properties")) {            
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
    
    public Service() {
        open();
    }

    protected Connection getConnection() {
        try {
            if (connection.isClosed()) {
                open();
            }
        }
        catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                       "SQL State: " + exception.getSQLState(),
                       exception);
        }

        return connection;
    }


    @Override
    public void close() throws SQLException {
        if (connection != null) connection.close();
    }
}
