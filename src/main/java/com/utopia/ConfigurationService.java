package com.utopia;

import java.io.InputStream;
import java.io.IOException;

import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access to configuration settings stored in config.properties.
 */
public class ConfigurationService
{
    private static final Logger LOGGER =
        Logger.getLogger(ConfigurationService.class.getName());

    private Properties properties = new Properties();
    
    public ConfigurationService() {
        try {
            InputStream input =
                ConfigurationService.class.getClassLoader().getResourceAsStream
                ("properties/configuration.properties");
            properties.load(input);
        }
        catch (IOException exception) {
            Assert.fail("Unable to load properties/configuration.properties.");
        }
    }

    public int getQuadSize() {
        final String quadSize = properties.getProperty("quad_size");
        Assert.assertNotNull(quadSize);
        return Integer.parseInt(quadSize);
    }

    public int getTileSize() {
        final String tileSize = properties.getProperty("tile_size");
        Assert.assertNotNull(tileSize);
        return Integer.parseInt(tileSize);
    }

    public String getAuthorisationToken() {
        final String token = properties.getProperty("authorisation_token");
        Assert.assertNotNull(token);
        return token;
    }
}
