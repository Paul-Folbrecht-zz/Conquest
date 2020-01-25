package com.osi.conquest.ui.renderers;


import com.osi.conquest.ConquestException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.City;
import com.osi.conquest.domain.MapSquare;
import com.osi.util.StringUtils;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class CityRenderer implements City.Renderer {
    protected static Image _image;
    protected Rectangle _markerRect = new Rectangle(0, 0, 4, 4);
    protected City _city;

    /**
     *
     */
    static {
        try {
            _image = PropertyManager.getImage(StringUtils.stripClassName(City.class));
        } catch (ConquestException e) {
            Logger.error("Could not find city image", e);
        }
    }

    /**
     *
     */
    public CityRenderer(City city) {
        _city = city;
    }

    /**
     *
     */
    public void render(Graphics2D graphics) {
        int x = (int) _city.getLocation().getX() * MapSquare.WIDTH;
        int y = (int) _city.getLocation().getY() * MapSquare.HEIGHT;

        graphics.drawImage(getImage(), x, y, null);
        renderPlayerMarker(graphics, x, y);
    }

    /**
     *
     */
    protected void renderPlayerMarker(Graphics2D graphics, int x, int y) {
        _markerRect.setLocation(x, y);
        graphics.setPaint(_city.getDisplayableColor());
        graphics.setStroke(new BasicStroke(1));
        graphics.fill(_markerRect);
        graphics.draw(_markerRect);
    }

    /**
     *
     */
    protected Image getImage() {
        return _image;
    }
}
