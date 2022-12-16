package com.utopia;

import java.io.IOException;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Servlet to add or retrieve tile images.
 */
public class TileImageServlet extends HttpServlet
{
    private TileService tileService = new TileService();
    private ConfigurationService configurationService =
        new ConfigurationService();    
    private AuthorisationService authorisationService =
        new AuthorisationService(configurationService);
    
    // @todo Generalise these iterators and re-use for AvatarImageServlet.
    
    /**
     * Iterate over tile IDs starting from the first tile ID and a count.
     */
    private class TileIdIterator implements Iterator<Long> {
        private long nextTileId;
        private int count;
        
        public TileIdIterator(long firstTileId, int count) {
            this.nextTileId = firstTileId;
            this.count = count;
        }

        public boolean hasNext() {
            return count > 0;
        }

        public Long next() {
            count--;
            return nextTileId++;
        }
    };

    /**
     * Iterate over tile IDs from a comma-separated list of tile codes.
     */
    private class TileCodeIterator implements Iterator<Long> {
        private final Map<String, Long> tileCodeToIdMap =
            tileService.getCodeToIdMap();

        private List<Long> tileIds = new ArrayList<Long>();
        
        public TileCodeIterator(final String idString, int maxCount) {
            final String[] tileIdsString = idString.split(",");
            for (final String tileIdString : tileIdsString) {
                final Long tileId = tileCodeToIdMap.get(tileIdString);
                Assert.assertNotNull("Unknown tile code " + tileIdString + ".",
                                     tileId);
                tileIds.add(tileId);
            }

            Assert.assertTrue(tileIds.size() <= maxCount);
        }

        public boolean hasNext() {
            return !tileIds.isEmpty();
        }

        public Long next() {
            final long nextTileId = tileIds.get(0);
            tileIds.remove(0);
            return nextTileId;
        }
    }
    
    /**
     * Add one or more tile images from a gride composite image
     * of tiles. The tiles are loaded from left-to-right, top-to-bottom.
     *
     * POST /tile.png?id=...[&x=...][&y=...][&width=...][&height=...]
     *
     * @param id First tile id or comma separated list of tile codes.
     * @param x, y, width, height Optional region in composite image to import
     * in pixels. If not specified imports the entire image as a series of tiles.
     */
    @Override public void doPost(HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException {

        authorisationService.checkAuthorised(request);
        
        // @todo Refactor this code with AvatarImageServlet.java.
        final String idString = request.getParameter("id");
        final String xString = request.getParameter("x");
        final String yString = request.getParameter("y");
        final String widthString = request.getParameter("width");
        final String heightString = request.getParameter("height");        
        
        final int tileSize = configurationService.getTileSize();
        
        BufferedImage compositeImage =
            ImageIO.read(request.getInputStream());
        final int x = xString != null? Integer.parseInt(xString) : 0;
        final int y = xString != null? Integer.parseInt(yString) : 0;
        final int width =
            widthString != null? Integer.parseInt(widthString) :
            compositeImage.getWidth();
        final int height =
            heightString != null? Integer.parseInt(heightString) :
            compositeImage.getHeight();            
        
        Assert.assertTrue(width % tileSize == 0);
        Assert.assertTrue(height % tileSize == 0);
        
        Assert.assertTrue("Expecting " + x + " + " + width + " <= " +
                          compositeImage.getWidth(),
                          x + width <= compositeImage.getWidth());
        Assert.assertTrue("Expecting " + y + " + " + height + " <= " +
                          compositeImage.getHeight(),
                          y + height <= compositeImage.getHeight());
        
        compositeImage =
            compositeImage.getSubimage(x, 
                                       y,
                                       width,
                                       height);
        
        final int columnCount = width / tileSize;
        final int rowCount = height / tileSize;
        final int count = rowCount * columnCount;
        
        Iterator<Long> tileIds = getTileIdIterator(idString, count);
        int column = 0;
        int row = 0;
        
        // Iterate from left->right, top->down writing out the tile
        // images from the composite image.
        while (tileIds.hasNext()) {
            final Long tileId = tileIds.next();
            final BufferedImage tileImage =
                compositeImage.getSubimage(column * tileSize,
                                           row * tileSize,
                                           tileSize,
                                           tileSize);
            ByteArrayOutputStream tileImageBytes =
                new ByteArrayOutputStream();
            ImageIO.write(tileImage, "png", tileImageBytes);
            tileService.setImage(tileId,
                                 tileImageBytes.toByteArray());
            
            column++;
            if (column >= columnCount) {
                column = 0;
                row++;
            }
        }
    }

    /**
     * Return an iterator to iterate over the tile IDs.
     */
    private Iterator<Long> getTileIdIterator(final String idString, int maxCount) {
        try {
            final long firstTileId = Long.parseLong(idString);
            return new TileIdIterator(firstTileId, maxCount);
        }
        catch (NumberFormatException exception) {
            return new TileCodeIterator(idString, maxCount);
        }
    }

    /**
     * Retrieve a tile image.
     *
     * GET /tile.png?id=...
     *
     * @param id tile ID or tile code
     * @todo Add support for retrieving multiple tiles in a single request.
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        response.setContentType("image/png");

        // @todo Users shouldn't retrieve tiles one by one.
        final String idString = request.getParameter("id");
        Long tileId;

        try {
            tileId = Long.parseLong(idString);
        }
        catch (NumberFormatException exception) {
            tileId = tileService.getTileId(idString);
            Assert.assertNotNull("Unknown tile code " + idString + ".",
                                 tileId);
        }

        final byte[] image = tileService.getImage(tileId);
        response.getOutputStream().write(image);
    }
}
