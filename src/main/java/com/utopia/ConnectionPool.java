package com.utopia;

import java.io.InputStream;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {

    private static final Logger LOGGER =
        Logger.getLogger(ConnectionPool.class.getName());
    
    private static HikariConfig config;
    private static HikariDataSource datasource;

    static {
        try {
            // Load PostgreSQL driver.
            Class.forName("org.postgresql.Driver");

            // Expecting a file with the following fields, e.g.
            // dataSourceClassName=org.postgresql.ds.PGSimpleDataSource
            // dataSource.user=utopia
            // dataSource.password=XXXXXX
            // dataSource.databaseName=utopia
            // dataSource.portNumber=5432
            // dataSource.serverName=127.0.0.1
            Properties properties = new Properties();
            InputStream input =
                ConnectionPool.class.getClassLoader().getResourceAsStream
                ("properties/hikari.properties");
            properties.load(input);
            
            // Support overloading values from configuration through
            // environment variables. This is useful for configuring
            // the application when running under Docker compose.
            {
                final String user = System.getenv("UTOPIA_DATABASE_USER");
                if (user != null) {
                    properties.setProperty("dataSource.user", user);
                }
            }

            {
                final String password =
                    System.getenv("UTOPIA_DATABASE_PASSWORD");
                if (password != null) {
                    properties.setProperty("dataSource.password", password);
                }
            }
                    
            {
                final String name = System.getenv("UTOPIA_DATABASE_NAME");
                if (name != null) {
                    properties.setProperty("dataSource.databaseName", name);
                }
            }

            {
                final String port = System.getenv("UTOPIA_DATABASE_PORT");
                if (port != null) {
                    properties.setProperty("dataSource.portNumber", port);
                }
            }

            {
                final String host = System.getenv("UTOPIA_DATABASE_HOST");
                if (host != null) {
                    properties.setProperty("dataSource.serverName", host);
                }
            }
            
            config = new HikariConfig(properties);
            datasource = new HikariDataSource(config);
        }
        catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Connection pool failure.",
                       exception);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }
    
    private ConnectionPool() {}
}
