package com.utopia;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet to create or retrieve user session.
 */
public class SessionServlet extends HttpServlet
{
   private static final Logger LOGGER =
       Logger.getLogger(SessionServlet.class.getName());

    private static final String COOKIE_NAME = "utopia-token";
    private static final String COOKIE_PATH = "/utopia";

    private MapService mapService = new MapService();    
    private TokenService tokenService = new TokenService();
    private TraversalService traversalService = new TraversalService();
    private UserService userService = new UserService();
    
    private Cookie findCookie(HttpServletRequest request) {
        final String contextPath = request.getContextPath();
        final Cookie [] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }

        return null;
    }

    private Cookie createCookie(final String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setPath(COOKIE_PATH);
        return cookie;
    }    
    
    /**
     * GET /session.json
     *
     * Find the user's session via cookie. Create a new session and avatar if not found.
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        response.setContentType("application/json; charset=utf-8");

        User user = null;
        final Cookie cookie = findCookie(request);
        if (cookie != null) {
            final String tokenString = cookie.getValue();
            final Token token = tokenService.findToken(tokenString);
            if (token != null) {
                user = userService.getUser(token.userId);
            }
            // @todo Log if not found.
            // @todo Update last seen time.
        }

        Map map = mapService.getMap();
        
        if (user == null) {
            user = userService.createNewUser();
            final String tokenString = tokenService.createToken(user.id);
            response.addCookie(createCookie(tokenString));
            traversalService.updateUserSeen
                (user.id,
                 TraversalService.calculateSeen(map.startX,
                                                map.startY,
                                                map.width,
                                                map.height,
                                                user.lastX,
                                                user.lastY));
        }
        
        Gson gson = new Gson();
        Session session = new Session();
        Session.User sessionUser = new Session.User();
        sessionUser.name = user.name;
        sessionUser.x = user.lastX;
        sessionUser.y = user.lastY;
        sessionUser.direction = user.lastDirection;
        session.user = sessionUser;
        session.map = map;
        session.traversal = traversalService.getUserSeen(user.id,
                                                         map.startX,
                                                         map.startY,
                                                         map.width,
                                                         map.height);
        
        response.getWriter().print(gson.toJson(session));
    }

    /**
     * PUT /session.json
     *
     * Update a user's last location.
     *
     * @todo Is this the best interface for doing this?
     */
    @Override public void doPut(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {
        
        response.setContentType("application/json; charset=utf-8");

        final Cookie cookie = findCookie(request);
        if (cookie != null) {
            final String tokenString = cookie.getValue();
            final Token token = tokenService.findToken(tokenString);

            Gson gson = new Gson();
            InputSession session = gson.fromJson(request.getReader(),
                                                 InputSession.class);

            // @todo Don't write every location update to the database.
            // Cache the request for a period of time and then update
            // later to minimise I/O.
            // @todo Validate that the user can move to that location.
            // @todo Rate limit movement.
            // @todo Validate direction.
            userService.updateUserLastLocation(token.userId,
                                               session.user.x,
                                               session.user.y,
                                               session.user.direction);
            // @todo Check that these coordinates are valid, i.e. they are
            // within the map and within the user's line of site. Another
            // way is calculating these server-side.
            traversalService.updateUserSeen(token.userId,
                                            session.traversal.seen);
        }
    }
}
