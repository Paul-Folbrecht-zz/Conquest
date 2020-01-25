package com.osi.conquest.ui.renderers;


import com.osi.conquest.ConquestException;
import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.GameController;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.Unit;
import com.osi.conquest.domain.impl.PlayerImpl;
import com.osi.conquest.domain.impl.unit.UnitImpl;
import com.osi.conquest.ui.MainWindow;
import com.osi.util.ImageUtils;
import com.osi.util.StringUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;


/**
 * @author Paul Folbrecht
 */
public class UnitRenderer implements Unit.Renderer {
    protected static HashMap _images = new HashMap();
    protected static HashMap _mirageImages = new HashMap();
    protected static Font _font;
    protected Unit _unit;
    protected Rectangle _markerRect = new Rectangle(0, 0, 4, 4);

    static {
        try {
            createImages();
            _font = new Font("", Font.PLAIN, 12);
        } catch (ConquestException e) {
            Logger.error("Error in initialization", e);
        }
    }

    public UnitRenderer(Unit unit) {
        _unit = unit;
    }

    /**
     *
     */
    public void renderMirage(Graphics2D graphics) {
        renderImpl(graphics, getMirageImage());
    }

    /**
     *
     */
    public void render(Graphics2D graphics) {
        renderImpl(graphics, getImage());
    }

    /**
     *
     */
    protected void renderImpl(Graphics2D graphics, Image image) {
        MapSquare square = getController().getMap().getSquareAt(_unit.getLocation());
        int x = (int) _unit.getLocation().getX() * MapSquare.WIDTH;
        int y = (int) _unit.getLocation().getY() * MapSquare.HEIGHT;

        if (_unit.isFacingRight()) {
            graphics.drawImage(image,x + MapSquare.WIDTH, y, x, y + MapSquare.HEIGHT, 0, 0, MapSquare.WIDTH, MapSquare.HEIGHT, null);
        } else {
            graphics.drawImage(image, x, y, x + MapSquare.WIDTH, y + MapSquare.HEIGHT, 0, 0, MapSquare.WIDTH, MapSquare.HEIGHT, null);
        }

        renderPlayerMarker(graphics);
        renderHitPointBar(graphics);

        if (_unit.isOnSentry()) {
            Rectangle2D metrics = _font.getStringBounds("z", graphics.getFontRenderContext());
//[[
            graphics.setFont(_font);
            graphics.setPaint(Color.white);
            graphics.drawString("z", square.getCenterPixelPoint().x, square.getCenterPixelPoint().y);
        }
    }

    /**
     *
     */
    protected void renderPlayerMarker(Graphics2D graphics) {
        int x = (int) _unit.getLocation().getX() * MapSquare.WIDTH;
        int y = (int) _unit.getLocation().getY() * MapSquare.HEIGHT;

        _markerRect.setLocation(x, y);
        graphics.setPaint(_unit.getOwner().getColor());
        graphics.setStroke(new BasicStroke(1));
        graphics.fill(_markerRect);
        graphics.draw(_markerRect);
    }

    /**
     *
     */
    protected void renderHitPointBar(Graphics2D graphics) {
        int x = (int) _unit.getLocation().getX() * MapSquare.WIDTH;
        int y = (int) _unit.getLocation().getY() * MapSquare.HEIGHT;
        double hitPointRatio = (double) _unit.getHitPoints() /
                (double) _unit.getUnitTypeData().getMaxHitPoints();
        int maxBarLength = (MapSquare.WIDTH / 2) - 1;
        Rectangle rect;
        Color color = Color.green;

        if (hitPointRatio <= .33f) color = Color.red;
        else if (hitPointRatio <= .67f) color = Color.yellow;

        rect = new Rectangle((x + MapSquare.WIDTH) - maxBarLength - 1, y, (int) (maxBarLength * hitPointRatio), 2);
        graphics.setPaint(color);
        graphics.setStroke(new BasicStroke(1));
        graphics.fill(rect);
        graphics.draw(rect);
    }

    /**
     *
     */
    protected static void createImages() throws ConquestException {
        // We only initialize the images for the first two players initially, as an optimization
        // (this is quite slow).  More will be added if a game with more than two players is started.
        for (int playerId = 0; playerId < 2; playerId++) {
            createImages(playerId);
        }
    }

    /**
     *
     */
    protected static void createImages(int playerId) throws ConquestException {
        _images.put(playerId, new HashMap());
        _mirageImages.put(playerId, new HashMap());

        for (int index = 0; index < UnitImpl.getClasses().size(); index++) {
            Class unitClass = (Class) UnitImpl.getClasses().get(index);
            HashMap images = (HashMap) _images.get(playerId);
            HashMap mirageImages = (HashMap) _mirageImages.get(playerId);
            Image image;

            image = PropertyManager.getImage(StringUtils.stripClassName(unitClass));
            image = ImageUtils.getTintedImage(MainWindow.getInstance().getGraphicsConfiguration(),
                    image, MapSquare.WIDTH, MapSquare.HEIGHT, PlayerImpl.getColor(playerId));
            images.put(unitClass, image);

            image = PropertyManager.getImage(StringUtils.stripClassName(unitClass));
            image = ImageUtils.getTintedImage(MainWindow.getInstance().getGraphicsConfiguration(),
                    image, MapSquare.WIDTH, MapSquare.HEIGHT, PlayerImpl.getColor(playerId));
            mirageImages.put(unitClass, ImageUtils.convertToGrayScale((BufferedImage) image));
        }
    }

    /**
     *
     */
    public static BufferedImage getImage(int playerId, Class unitClass) {
        HashMap images = (HashMap) _images.get(playerId);
        BufferedImage image = null;

        if (images == null) {
            try {
                createImages(playerId);
            } catch (ConquestException e) {
                // Downgrade the exception to runtime because createImages() can only fail if the image
                // files can't be found, and if the static initialization completed they must be there.
                throw new ConquestRuntimeException(e);
            }
            images = (HashMap) _images.get(playerId);
        }

        if (images == null) throw new ConquestRuntimeException("No images for player Id " + playerId);
        image = (BufferedImage) images.get(unitClass);
        if (image == null) throw new ConquestRuntimeException("No " + unitClass + " image for player " + playerId);

        return image;
    }

    /**
     *
     */
    protected BufferedImage getImage() {
        return getImage(_unit.getOwner().getId(), _unit.getClass());
    }

    /**
     *
     */
    protected BufferedImage getMirageImage() {
        HashMap images = (HashMap) _mirageImages.get(_unit.getOwner().getId());
        BufferedImage image = null;

        if (images != null) image = (BufferedImage) images.get(_unit.getClass());
        if (image == null) throw new ConquestRuntimeException("No " + _unit.getClass() + " mirage image for player " + _unit.getOwner().getId());

        return image;
    }

    /**
     *
     */
    protected GameController getController() {
        return ConquestFactory.getController();
    }
}
