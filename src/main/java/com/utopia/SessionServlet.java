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

    private TokenService tokenService = new TokenService();
    private UserService userService = new UserService();
    private MapService mapService = new MapService();
    
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
            user = userService.getUser(token.userId);
            // @todo Log if not found.
            // @todo Update last seen time.
        }

        if (user == null) {
            user = userService.createNewUser();
            final String tokenString = tokenService.createToken(user.id);
            response.addCookie(createCookie(tokenString));
        }
        
        Gson gson = new Gson();
        Session session = new Session();
        Session.User sessionUser = new Session.User();
        sessionUser.name = user.name;
        sessionUser.x = user.lastX;
        sessionUser.y = user.lastY;
        session.user = sessionUser;
        session.map = mapService.getMap();
        
        response.getWriter().print(gson.toJson(session));
    }
}
