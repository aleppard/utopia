package com.utopia;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Servlet to add or retrieve a tile image.
 */
public class TileImageServlet extends HttpServlet
{
    /**
     * Add a new tile image.
     *
     * POST /tile.png?id=...
     */
    @Override public void doPost(HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException {

        // @todo Secure this end-point.
        final String idString = request.getParameter("id");
        try {
            long id = Long.parseLong(idString);
            byte[] image = IOUtils.toByteArray(request.getInputStream());

            // @todo Check image format.
            // @todo Check image is the right size.
            TileService service = new TileService();
            service.setImage(id, image);
        }
        catch (NumberFormatException exception) {
            // @todo
        }
    }

    /**
     * Retrieve a tile image.
     *
     * GET /tile.png?id=...
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        response.setContentType("image/png");

        // @todo Users shouldn't retrieve tiles one by one.
        final String idString = request.getParameter("id");
        try {
            long id = Long.parseLong(idString);
            
            TileService service = new TileService();
            byte[] image = service.getImage(id);
            response.getOutputStream().write(image);
        }
        catch (NumberFormatException exception) {
            // @todo
        }
    }
}
