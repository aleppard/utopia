package com.utopia;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom assertions class.
 */
public class Assert {
    
    private static final Logger LOGGER =
        Logger.getLogger(Assert.class.getName());
    
    public static void fail() {
        fail(null);
    }

    public static void fail(final String message) {
        try {
            throw new RuntimeException();
        }
        catch (Exception exception) {
            String logMessage;
            if (message != null) {
                logMessage = "Assertion failure: " + message;
            }
            else {
                logMessage = "Assertion failure.";
            }
            LOGGER.log(Level.SEVERE, logMessage, exception);
            throw exception;
        }
    }
    
    public static void assertTrue(boolean value) {
        assertTrue(null, value);
    }

    public static void assertTrue(final String message, boolean value) {
        if (!value) fail(message);
    }

    public static void assertNotNull(Object object) {
        if (object == null) fail();
    }

    public static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            fail("expected " + expected + " found " + actual);
        }
    }
}
