package com.utopia;

import java.sql.Connection;
import java.sql.SQLException;

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
            config = new HikariConfig("/properties/database.properties");
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
