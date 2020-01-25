package com.osi.conquest.domain;


import java.awt.*;
import java.io.Serializable;


/**
 * @author Paul Folbrecht
 */
public interface MapSquare extends Serializable, Cloneable {
    public static final int ILLEGAL_MOVE = 1000;
    public static final int WIDTH = 22;
    public static final int HEIGHT = 20;

    public static interface SquareTypeData {
        public String getName();

        public Color getWorldMapColor();
    }

    public static interface Renderer {
        public void render(Graphics2D graphics);

        public void render(Graphics2D graphics, boolean paintUnit);

        public void renderThumbnail(Graphics2D graphics);

        public void setHighlight(boolean highlight);
    }

    public Object clone() throws CloneNotSupportedException;

    public Renderer getRenderer();

    public SquareTypeData getSquareTypeData();

    public Point getLocation();

    public Rectangle getArea();

    public Point getCenterPixelPoint();

    public OwnableObject getObject();

    public Unit getUnit();

    public Unit getHiddenUnit();

    public City getCity();

    public boolean isWater();

    public boolean isCoastal();

    public boolean isVisibleToPlayer(Player player);

    public void unHide(Player player);

    public void setUnit(Unit unit);

    public void setCity(City city);

    public void setHiddenUnit(Unit unit);

    public void setLocation(Point location);

    public void setHidden(boolean hidden);

    public void highlight();

    public void unHighlight();
}
