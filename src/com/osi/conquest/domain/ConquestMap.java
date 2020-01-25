package com.osi.conquest.domain;


import java.awt.*;
import java.io.Serializable;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public interface ConquestMap extends Serializable, Cloneable {
    public MapSquare getSquareAt(Point location);

    public MapSquare fastGetSquareAt(int x, int y);

    public MapSquare getSquareAtPixel(Point location);

    public MapSquare findUnoccupiedSquareWithinRadius(Unit unit, int radius);

    public List getOwnableObjectsWithinRadius(Point location, int radius);

    public Dimension getSize();

    public Dimension getSizeInPixels();

    public City getRandomCoastalCity();

    public City getStartCity(Player player);

    public City findClosestFriendlyCity(Unit unit);

    public int getRange(Point start, Point end);

    public MapParameters getParameters();

    public Thread init();

    public void setCitiesAndUnits(Player[] players);

    public void uncoverTerrain(Point center, int range, Player player);

    public void paint(Graphics2D graphics, boolean paintUnits);
}
