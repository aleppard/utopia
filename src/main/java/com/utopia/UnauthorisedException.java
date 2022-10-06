package com.utopia;

/**
 * An HTTP request fails authorisation (401).
 *
 * Calls that require authorisation such as those that change game assets
 * must be authenticated using a bearer token. Pass utopia.authorisation_token
 * as the bearer token. To do this set the "Authorization" header in the
 * request to "Bearer TOKEN".
 */
public class UnauthorisedException extends RuntimeException {
    public UnauthorisedException() {
        super("Unauthorised. Pass the utopia.authorisation_token as the bearer token.");
    }
};
