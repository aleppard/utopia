package com.utopia;

import java.io.InputStream;
import java.io.IOException;

import java.util.logging.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

// MYTODO rename to file servlet
/**
 * Game servlet.
 */
public class GameServlet extends HttpServlet
{
    private static final Logger LOGGER =
        Logger.getLogger(GameServlet.class.getName());
    
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        // Ignore /utopia/ prefix if present.
        final String prefix = "/utopia";
        String uri = request.getRequestURI();
        if (uri.startsWith(prefix)) {
            uri = uri.substring(prefix.length());
        }
        
        if (uri.length() == 1) {
            getIndex(response);
        }
        else if (uri.startsWith("/js/")) {
            getJavascript(uri, response);
        }
        else if (uri.startsWith("/images/")) {
            getImages(uri, response);
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);            
        }
    }
    
    private void getIndex(HttpServletResponse response)
        throws IOException {
        response.setContentType("text/html; charset=utf-8");
        InputStream input =
            GameServlet.class.getClassLoader().getResourceAsStream("web/index.html");
        ByteStreams.copy(input, response.getOutputStream());
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void getJavascript(final String uri,
                               HttpServletResponse response)
        throws IOException {
        response.setContentType("application/javascript; charset=utf-8");
        InputStream input =
            GameServlet.class.getClassLoader().getResourceAsStream("web" + uri);
        ByteStreams.copy(input, response.getOutputStream());
        response.setStatus(HttpServletResponse.SC_OK);        
    }

    private void getImages(final String uri,
                           HttpServletResponse response)
        throws IOException {
        response.setContentType("image/png");
        InputStream input =
            GameServlet.class.getClassLoader().getResourceAsStream("web" + uri);
        ByteStreams.copy(input, response.getOutputStream());
        response.setStatus(HttpServletResponse.SC_OK);        
    }
}
