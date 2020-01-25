package com.osi.conquest.domain;


import java.awt.*;
import java.io.Serializable;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public interface UnitPath extends Cloneable, Serializable {
    public Object clone() throws CloneNotSupportedException;

    public void reset();

    public int getLength();

    public MapSquare getNextSquare();

    public MapSquare getLastSquare();

    public Rectangle getBoundingRect();

    public List getPath();

    public void moveToPreviousSquare();

    public boolean calculatePath(boolean skipOccupiedSquares);

    public void paint(Graphics2D graphics);

    public void erase(Graphics2D graphics);

    public void add(UnitPath other);
}
