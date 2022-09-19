package com.utopia;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create new users.
 */
public class CreateNewUserService extends Service
{
    private UserService userService;
    private AvatarService avatarService = new AvatarService();
    private NameGenerator nameGenerator = new NameGenerator();
    
    public CreateNewUserService(UserService userService) {
        this.userService = userService;
    }
    
    public User create() {
        final String name = nameGenerator.generateName();
        // @todo Should check this is traverseable.
        // @todo Create new users in different locations?
        final int x = 6;
        final int y = 6;
        final String direction = "east"; // @todo Create direction enum.
        final long avatarId = avatarService.getRandomUserAvatarId();
        return userService.createNewUser(name, x, y, direction, avatarId);
    }
}
