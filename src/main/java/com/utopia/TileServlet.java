package com.utopia;

import java.io.IOException;

import java.util.List;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet to add new tiles.
 */
public class TileServlet extends HttpServlet
{
    private TileService service = new TileService();
    
    /**
     * Add new tiles.
     *
     * POST /tile.json
     */
    @Override public void doPost(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        // @todo Secure this end-point.
        response.setContentType("application/json; charset=utf-8");

        final ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        final List<Tile> tiles =
            mapper.readValue(request.getReader(),
                             new TypeReference<List<Tile>>() {
                             });
        service.add(tiles);
    }
}
