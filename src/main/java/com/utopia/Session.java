package com.utopia;

import java.util.ArrayList;
import java.util.List;

/**
 * @todo Rename to OutputSession.
 */
public class Session
{
    public static class User {
        public String name;
        public int x;
        public int y;
        public String direction;
    }

    public User user;
    public Map map;

    // @todo Perhaps this should be under User?
    public Traversal traversal;
}
