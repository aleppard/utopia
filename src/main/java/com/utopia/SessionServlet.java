package com.utopia;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet to create or retrieve user session.
 */
public class SessionServlet extends HttpServlet
{
   private static final Logger LOGGER =
       Logger.getLogger(SessionServlet.class.getName());

    private static final String COOKIE_NAME = "utopia-token";
    private static final String COOKIE_PATH = "/";

    private ConfigurationService configurationService =
        new ConfigurationService();
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
     * POST /session.json&screen_width=...&screen_height=...
     *
     * Find the user's session via cookie. Create a new session and avatar if 
     * not found. Return the user's location and direction, immediate map 
     * and traversal surrounds.
     *
     * The screen_width and screen_height, in tiles, of the client's screen is 
     * passed to determine the initial size of the map returned.
     */
    @Override public void doPost(HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException {

        response.setContentType("application/json; charset=utf-8");

        final int screenWidth =
            Integer.parseInt(request.getParameter("screen_width"));
        final int screenHeight =
            Integer.parseInt(request.getParameter("screen_height"));
        
        User user = null;
        boolean isNewUser = true;
        
        final Cookie cookie = findCookie(request);
        if (cookie != null) {
            final String tokenString = cookie.getValue();
            final Token token = tokenService.findToken(tokenString);
            if (token != null) {
                user = userService.getUser(token.userId);
                isNewUser = false;
            }
            // @todo Log if not found.
            // @todo Update last seen time.
        }
        
        if (user == null) {
            user = userService.createNewUser();
            final String tokenString = tokenService.createToken(user.id);
            response.addCookie(createCookie(tokenString));
        }

        // Figure out the starting map region to return based on the user's
        // avatar position and screen size.
        final Region startRegion =
            LoadRegion.find(mapService.getBounds(),
                            configurationService.getQuadSize(),
                            user.lastX,
                            user.lastY,
                            screenWidth,
                            screenHeight);
        
        if (isNewUser) {
            // @todo For new users we do this here and on the client.
            // We shouldn't do it for both.
            traversalService.updateUserSeen
                (user.id,
                 TraversalService.calculateSeen(startRegion.startX,
                                                startRegion.startY,
                                                startRegion.width,
                                                startRegion.height,
                                                user.lastX,
                                                user.lastY));
        }

        Session session = new Session();
        Session.User sessionUser = new Session.User();
        sessionUser.name = user.name;
        sessionUser.x = user.lastX;
        sessionUser.y = user.lastY;
        sessionUser.direction = user.lastDirection;
        session.mapBounds = mapService.getBounds();
        session.user = sessionUser;
        session.map = mapService.getMap(startRegion.startX,
                                        startRegion.startY,
                                        startRegion.width,
                                        startRegion.height);
        session.traversal = traversalService.getUserSeen(user.id,
                                                         startRegion.startX,
                                                         startRegion.startY,
                                                         startRegion.width,
                                                         startRegion.height);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), session);
    }

    /**
     * GET /session.json?start_x=...&start_x=...&width=...&height=...
     *
     * Retrieve the map and the user's traversal for the given region.
     *
     * @todo Support disparate regions?
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {
        
        response.setContentType("application/json; charset=utf-8");

        // @todo Move this to a Region class and pass that around instead.
        final int startX = Integer.parseInt(request.getParameter("start_x"));
        final int startY = Integer.parseInt(request.getParameter("start_y"));
        final int width = Integer.parseInt(request.getParameter("width"));
        final int height = Integer.parseInt(request.getParameter("height"));

        Session session = new Session();
        session.map = mapService.getMap(startX, startY, width, height);

        // If we can relate it to a specific user then return their traversal
        // at the location too.
        final Cookie cookie = findCookie(request);
        if (cookie != null) {
            final String tokenString = cookie.getValue();
            final Token token = tokenService.findToken(tokenString);
            
            session.traversal = traversalService.getUserSeen(token.userId,
                                                             startX,
                                                             startY,
                                                             width,
                                                             height);
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), session);
    }
    
    /**
     * PUT /session.json
     *
     * Update a user's last location, direction and traversal.
     *
     * @todo Is this the best interface for doing this?
     * @todo Retrieving new tile data and updating user location happen at
     * the same time. Can we combine these operations?
     */
    @Override public void doPut(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {
        
        response.setContentType("application/json; charset=utf-8");

        final Cookie cookie = findCookie(request);
        if (cookie != null) {
            final String tokenString = cookie.getValue();
            final Token token = tokenService.findToken(tokenString);

            ObjectMapper mapper = new ObjectMapper();
            InputSession session = mapper.readValue(request.getReader(),
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
