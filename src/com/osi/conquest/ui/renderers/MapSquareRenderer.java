package com.osi.conquest.ui.renderers;


import com.osi.conquest.ConquestException;
import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.*;
import com.osi.conquest.domain.impl.mapsquare.*;
import com.osi.conquest.ui.MainWindow;
import com.osi.util.StringUtils;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.util.HashMap;


/**
 * @author Paul Folbrecht
 */
public class MapSquareRenderer implements MapSquare.Renderer {
    public static final int THUMBNAIL_SQUARE_WIDTH = 2;
    public static final int THUMBNAIL_SQUARE_HEIGHT = 2;

    protected static HashMap _images = new HashMap();
    protected MapSquare _square;
    protected transient Rectangle _rect;
    protected boolean _isHighlighted = false;
    protected Rectangle _thumbnailRect = null;

    /**
     *
     */
    static {
        try {
            _images.put(Water.class, PropertyManager.getImage(StringUtils.stripClassName(Water.class)));
            _images.put(Plain.class, PropertyManager.getImage(StringUtils.stripClassName(Plain.class)));
            _images.put(Swamp.class, PropertyManager.getImage(StringUtils.stripClassName(Swamp.class)));
            _images.put(Mountain.class, PropertyManager.getImage(StringUtils.stripClassName(Mountain.class)));
            _images.put(Forest.class, PropertyManager.getImage(StringUtils.stripClassName(Forest.class)));
            _images.put(City.class, PropertyManager.getImage(StringUtils.stripClassName(City.class)));
        } catch (ConquestException e) {
            Logger.error("Could not find images", e);
        }
    }

    /**
     *
     */
    public MapSquareRenderer(MapSquare square) {
        _square = square;
    }

    /**
     *
     */
    public void setHighlight(boolean highlight) {
        _isHighlighted = highlight;
    }

    /**
     *
     */
    public void render(Graphics2D graphics) {
        render(graphics, true);
    }

    /**
     *
     */
    public void render(Graphics2D graphics, boolean renderUnit) {
        int x = (int) _square.getLocation().getX() * MapSquare.WIDTH;
        int y = (int) _square.getLocation().getY() * MapSquare.HEIGHT;

        if (_square.isVisibleToPlayer(getController().getCurrentPlayer())) {
            if (_square.getCity() != null) _square.getCity().getRenderer().render(graphics);
            else graphics.drawImage(getImage(), x, y, null);

            renderHighlight(graphics);
            if (renderUnit) renderUnit(graphics);
        } else {
            // Cache this as an optimization because our location doesn't change.
            if (_rect == null) _rect = new Rectangle(x, y, MapSquare.WIDTH, MapSquare.HEIGHT);
            // Draw a black square.
            graphics.setPaint(Color.black);
            graphics.setStroke(new BasicStroke(1));
            graphics.fill(_rect);
            graphics.draw(_rect);
        }
    }

    /**
     *
     */
    public void renderThumbnail(Graphics2D graphics) {
        Point location = _square.getLocation();

        if (_square.isVisibleToPlayer(getController().getCurrentPlayer())) graphics.setPaint(getSquareColor());
        else graphics.setPaint(Color.black);

        if (_thumbnailRect == null) {
            // Cache this as an optimization.
            _thumbnailRect = new Rectangle(0, 0, THUMBNAIL_SQUARE_WIDTH, THUMBNAIL_SQUARE_HEIGHT);
            _thumbnailRect.setLocation(location.x * THUMBNAIL_SQUARE_WIDTH, location.y * THUMBNAIL_SQUARE_HEIGHT);
        }
        graphics.fill(_thumbnailRect);
        graphics.draw(_thumbnailRect);
    }

    /**
     *
     */
    protected void renderUnit(Graphics2D graphics) {
        if (_square.getCity() == null) {
            Player player = getController().getCurrentPlayer();

if (_square.getUnit() != null && _square.getUnit().getOwner() != player) {
    Logger.info("Unit at " + _square + ": " + _square.getUnit().toString());
    player.dump();
    Logger.info("player.isObjectSighted(): " + player.isObjectSighted(_square.getUnit(), _square.getLocation()));
}
            if (_square.getUnit() != null && (_square.getUnit().getOwner() == player || MainWindow.getInstance().getState() == MainWindow.STATE_PLAYING_MOVEMENT_REPORTS)) {
                _square.getUnit().getRenderer().render(graphics);
            } else if (_square.getUnit() != null && player.isObjectSighted(_square.getUnit(), _square.getLocation())) {
// This is getting hit when a unit was previously visible but is no longer, because Player._lastSightedObjects is not cleared between turns.
// In addition to tracking what was once visible (_lastSightedObjects), there needs to be a turn-initted data structure of whether any unit at
// x, y is presently sighted by any unit of the current player.
                _square.getUnit().getRenderer().render(graphics);
            } else {
                Unit unit = (Unit) player.getLastSightedObject(_square.getLocation());
                if (unit != null) unit.getRenderer().renderMirage(graphics);
            }
        }
    }

    /**
     *
     */
    protected void renderHighlight(Graphics2D graphics) {
        if (_isHighlighted) {
            Rectangle rect = new Rectangle(_square.getLocation().x * MapSquare.WIDTH,
                    _square.getLocation().y * MapSquare.HEIGHT, MapSquare.WIDTH - 1, MapSquare.HEIGHT - 1);

            graphics.setPaint(Color.white);
            graphics.setStroke(new BasicStroke(1));
            graphics.draw(rect);
        }
    }

    /**
     *
     */
    protected Color getSquareColor() {
        if (_square.getCity() != null) return _square.getCity().getDisplayableColor();
        else return _square.getSquareTypeData().getWorldMapColor();
    }

    /**
     *
     */
    protected Image getImage() {
        Image image = (Image) _images.get(_square.getClass());

        if (image == null) throw new ConquestRuntimeException("MapSquareImpl: No image for " + getClass());

        return image;
    }

    /**
     *
     */
    protected GameController getController() {
        return ConquestFactory.getController();
    }
}
