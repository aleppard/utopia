package com.utopia;

import java.time.OffsetDateTime;

/**
 *
 */
public class User
{
    public long id;
    public OffsetDateTime createdTime;
    public OffsetDateTime lastSeenTime;
    public int lastX;
    public int lastY;
    public String lastDirection;
    public String name;
    public String email;
}
