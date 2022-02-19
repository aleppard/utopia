package com.utopia;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet to retrieve map.
 */
public class MapServlet extends HttpServlet
{
    /**
     * Retrieve map meta-data.
     *
     * GET /map.json
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        // @todo A better approach would be to use gRPC and cache 8x8 chunks
        // of the map in memory as protocol buffers to remove any I/O or
        // serialisation. We could then just write out chunks directly from
        // memory in a compact binary format.
        response.setContentType("application/json; charset=utf-8");

        MapService service = new MapService();
        Map map = service.getMap();
        Gson gson = new Gson();
        response.getWriter().print(gson.toJson(map));
    }
}
