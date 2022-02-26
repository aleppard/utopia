package com.utopia;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet to add a new tile.
 */
public class TileServlet extends HttpServlet
{
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

        Gson gson = new Gson();
        Tile tile = gson.fromJson(request.getReader(), Tile.class);
        TileService service = new TileService();
        service.add(tile);
    }
}
