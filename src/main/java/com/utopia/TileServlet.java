package com.utopia;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet to add a new tile.
 */
public class TileServlet extends HttpServlet
{
    private TileService service = new TileService();
    
    /**
     * Add a new tile.
     *
     * POST /tile.json
     */
    @Override public void doPost(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        // @todo Secure this end-point.
        response.setContentType("application/json; charset=utf-8");

        ObjectMapper mapper = new ObjectMapper();        
        Tile tile = mapper.readValue(request.getReader(), Tile.class);
        service.add(tile);
    }
}
