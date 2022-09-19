package com.utopia;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.logging.Logger;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet to add or retrieve avatar images.
 */
public class AvatarImageServlet extends HttpServlet
{
    private static final Logger LOGGER =
        Logger.getLogger(AvatarImageServlet.class.getName());
    
    private AvatarService avatarService = new AvatarService();    
    private ConfigurationService configurationService =
        new ConfigurationService();    

    // Number of tile rows in a composite avatar image (south, west, east & norht).
    private final int AVATAR_ROW_TILE_COUNT = 4;

    // Number of tile columns in a composite avatar image (frames).
    private final int AVATAR_COLUMN_TILE_COUNT = 3;
    
   /**
     * Add one or more avatar grid composite images from a grid composite image
     * of avatar composites. The avatars are loaded from left-to-right,
     * top-to-bottom. Each composite avatar image should be 4 rows x
     * 3 columns containing the following:
     *
     * The first row is frames of the avatar facing south.
     * The second row is frames of the avatar facing west.
     * The third row is frames of the avatar facing east.
     * The fourth row is frames of the avatar facing north.
     *
     * For example if the image contains 9 avatars:
     *
     * A B C
     * D E F
     * G H I
     *
     * Each avatar would be further broken into
     *
     * S1 S2 S3
     * W1 W2 W3
     * E1 E2 E3
     * N1 N2 N3
     *
     * POST /avatar.png?id=...&count=...
     */
    @Override public void doPost(HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException {

        // @todo Secure this end-point.
        final String idString = request.getParameter("id");
        final String countString = request.getParameter("count");
        
        try {
            long firstAvatarId = Long.parseLong(idString);
            final int count = Integer.parseInt(countString);
            final int tileSize = configurationService.getTileSize();
            final int avatarWidth = AVATAR_COLUMN_TILE_COUNT * tileSize;
            final int avatarHeight = AVATAR_ROW_TILE_COUNT * tileSize;
            
            final BufferedImage compositeImage =
                ImageIO.read(request.getInputStream());
            Assert.assertTrue(compositeImage.getWidth() % avatarWidth == 0);
            Assert.assertTrue(compositeImage.getHeight() % avatarHeight == 0);

            final int columnCount = compositeImage.getWidth() / avatarWidth;
            final int rowCount = compositeImage.getHeight() / avatarHeight;            
            Assert.assertTrue(count <= rowCount * columnCount);

            int column = 0;
            int row = 0;

            // Iterate from left->right, top->down writing out the avatar
            // images from the composite image.
            for (long avatarId = firstAvatarId; avatarId < (firstAvatarId + count);
                 avatarId++) {
                final BufferedImage avatarImage =
                    compositeImage.getSubimage(column * avatarWidth,
                                               row * avatarHeight,
                                               avatarWidth,
                                               avatarHeight);
                ByteArrayOutputStream avatarImageBytes =
                    new ByteArrayOutputStream();
                ImageIO.write(avatarImage, "png", avatarImageBytes);
                avatarService.setImage(avatarId,
                                       avatarImageBytes.toByteArray());

                column++;
                if (column >= columnCount) {
                    column = 0;
                    row++;
                }
            }
        }
        catch (NumberFormatException exception) {
            // @todo
        }
    }
    
   /**
     * Retrieve composite avatar image.
     *
     * GET /avatar.png?id=...
     */
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        response.setContentType("image/png");

        final String idString = request.getParameter("id");
        try {
            long id = Long.parseLong(idString);
            
            AvatarService service = new AvatarService();
            byte[] image = service.getImage(id);
            response.getOutputStream().write(image);
        }
        catch (NumberFormatException exception) {
            // @todo
        }
    }
}
