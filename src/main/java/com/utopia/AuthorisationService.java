package com.utopia;

import java.io.InputStream;
import java.io.IOException;

import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Check authorisation of a call. Currently only calls to modify the
 * game assets (e.g. map, tiles, etc) require authorisation.
 */
public class AuthorisationService
{
    private static final Logger LOGGER =
        Logger.getLogger(AuthorisationService.class.getName());

    private ConfigurationService configurationService;

    public AuthorisationService() {
        this.configurationService = new ConfigurationService();
    }
    
    public AuthorisationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
        
    private void checkBearerToken(final String bearerToken) {
        if (!bearerToken.equals(configurationService.getAuthorisationToken())) {
            throw new UnauthorisedException();
        }
    }

    /**
     * Check that the request is authorised. Currently to authorise
     * a request the caller should just pass the utopia.authentication_token
     * as the bearer value in the Authorization header, i.e.
     *
     * Authorization: BEARER <utopia.authorisation_token>
     */
    public void checkAuthorised(HttpServletRequest request) {
        final String authorisationHeader = request.getHeader("Authorization");
        if (authorisationHeader == null ||
            !authorisationHeader.toUpperCase().startsWith("BEARER ")) {
            throw new UnauthorisedException();
        }

        final String bearerToken =
            authorisationHeader.substring(new String("BEARER ").length());
        checkBearerToken(bearerToken);
    }
}
