package com.utopia;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet to retrieve map.
 */
public class MapServlet extends HttpServlet
{
   private static final Logger LOGGER =
       Logger.getLogger(MapServlet.class.getName());

    private MapService service = new MapService();
    private AuthorisationService authorisationService =
        new AuthorisationService();
    
    /**
     * Retrieve full map:
     *
     * GET /map.json
     *
     * Retrieve part of map:
     *
     * GET /map.json?start_x=...&start_x=...&width=...&height=...
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        // @todo A better approach would be to use gRPC and cache 16x16 chunks
        // of the map in memory as protocol buffers to remove any I/O or
        // serialisation. We could then just write out chunks directly from
        // memory in a compact binary format. Or we coud just use bson.
        response.setContentType("application/json; charset=utf-8");

        Map map;
        
        if (request.getParameter("start_x") != null) {
            final String startX = request.getParameter("start_x");
            final String startY = request.getParameter("start_y");
            final String width = request.getParameter("width");
            final String height = request.getParameter("height");            
            map = service.getMap(Integer.parseInt(startX),
                                 Integer.parseInt(startY),
                                 Integer.parseInt(width),
                                 Integer.parseInt(height));
        }
        else {
            map = service.getMap();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), map);
    }

    /**
     * POST /map.json
     *
     * Upload a map which can be a list of tile IDs or tile codes for each 
     * square in the map.
     */
    @Override public void doPost(HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException {

        authorisationService.checkAuthorised(request);

        ObjectMapper mapper = new ObjectMapper();        
        InputMap map = mapper.readValue(request.getReader(), InputMap.class);
        service.setMap(map);
    }
}
