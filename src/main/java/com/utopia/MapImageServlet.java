package com.utopia;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.awt.Graphics2D;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet to retrieve map as image.
 */
public class MapImageServlet extends HttpServlet
{
    private ConfigurationService configurationService =
        new ConfigurationService();
    private MapService mapService = new MapService();
    private TileService tileService = new TileService();

    /**
     * Return a list of the unique tile IDs in the given map.
     */
    private static List<Long> getUniqueTileIds(final Map map) {
        HashSet<Long> uniqueTileIds = new HashSet<>();
        
        for (int x = 0; x < map.width; x++) {
            for (int y = 0; y < map.height; y++) {
                final List<Long> tileIds = map.tiles[y][x];
                for (final Long tileId : tileIds) {
                    uniqueTileIds.add(tileId);
                }
            }
        }

        return new ArrayList<Long>(uniqueTileIds);
    }

    /**
     * Load all the tile IDs in the list and return a map mapping
     * each tile ID to each image.
     */
    private HashMap<Long, BufferedImage> createTileIdToImageMap
        (final List<Long> tileIds) throws IOException {
        HashMap<Long, BufferedImage> tileIdToImageMap = new HashMap<>();

        for (final Long tileId : tileIds) {
            final byte[] imageBytes = tileService.getImage(tileId);
            ByteArrayInputStream imageByteArrayInputStream = new
                ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(imageByteArrayInputStream);
            tileIdToImageMap.put(tileId, image);
        }
            
        return tileIdToImageMap;
    }
    
    /**
     * Retrieve full map as image.
     *
     * GET /map.png
     *
     * @todo Add ability to specify region of map and add limit to
     * how big an image we can return.
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        response.setContentType("image/png");

        final int tileSize = configurationService.getTileSize();
       
        Map map = mapService.getMap(mapService.getStartX(),
                                    mapService.getStartY(),
                                    mapService.getWidth(),
                                    mapService.getHeight());
        List<Long> uniqueTileIds = getUniqueTileIds(map);
        HashMap<Long, BufferedImage> tileIdToImageMap =
            createTileIdToImageMap(uniqueTileIds);
        
        // @todo Separate function.
        BufferedImage image =
            new BufferedImage(map.width * tileSize,
                              map.height * tileSize,
                              BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        
        for (int x = 0; x < map.width; x++) {
            for (int y = 0; y < map.height; y++) {
                final List<Long> tileIds = map.tiles[y][x];
                for (final Long tileId : tileIds) {
                    final BufferedImage tileImage =
                        tileIdToImageMap.get(tileId);
                    graphics.drawImage(tileImage,
                                       x * tileSize,
                                       y * tileSize,
                                       tileSize,
                                       tileSize,
                                       null);
                }
            }
        }
                                         
        ImageIO.write(image, "png", response.getOutputStream());
    }
}
