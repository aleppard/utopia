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
 * Servlet to add new tile connectors.
 */
public class TileConnectorServlet extends HttpServlet
{
    private TileConnectorService service = new TileConnectorService();
    
    /**
     * Add new tile connections.
     *
     * POST /tile_connection.json
     */
    @Override public void doPost(HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException {

        // @todo Secure this end-point.
        response.setContentType("application/json; charset=utf-8");
        
        final ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        final List<TileConnector> tileConnectors =
            mapper.readValue(request.getReader(),
                             new TypeReference<List<TileConnector>>() {
                             });
        service.add(tileConnectors);
    }
}
