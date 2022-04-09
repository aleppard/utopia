package com.utopia;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class InputSession
{
    public static class User {
        public int x;
        public int y;
        public String direction;
    }

    // @todo We should validate what the user gives us.
    public static class Traversal {
        public Integer[][] seen;
    }
    
    public User user;
    public Traversal traversal;
}
