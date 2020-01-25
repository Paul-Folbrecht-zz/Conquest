package com.osi.conquest.domain.impl.mapsquare;


import com.osi.conquest.domain.*;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public abstract class MapSquareImpl implements MapSquare {
    private static final long serialVersionUID = 1L;
    protected Point _location;
    protected boolean[] _hiddenFlags;
    protected City _city;
    protected transient Unit _unit;
    protected transient Unit _hiddenUnit;
    protected transient MapSquare.Renderer _renderer;

    /**
     *
     */
    public MapSquareImpl() {
    }

    public abstract MapSquare.SquareTypeData getSquareTypeData();

    /**
     *
     */
    public String toString() {
        return getClass().getName().substring(getClass().getName().lastIndexOf('.')) + " at " + getLocation().x + ", " + getLocation().y;
    }

    /**
     *
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     *
     */
    public MapSquare.Renderer getRenderer() {
        if (_renderer == null) _renderer = ConquestFactory.createMapSquareRenderer(this);
        return _renderer;
    }

    /**
     *
     */
    public Point getLocation() {
        return _location;
    }

    /**
     *
     */
    public Rectangle getArea() {
        return new Rectangle(_location.x, _location.y, 1, 1);
    }

    /**
     * @todo UI knowledge here.
     */
    public Point getCenterPixelPoint() {
        return new Point((_location.x * MapSquare.WIDTH) + (MapSquare.WIDTH / 2), (_location.y * MapSquare.HEIGHT) + (MapSquare.HEIGHT / 2));
    }

    /**
     *
     */
    public OwnableObject getObject() {
        if (_city != null) return _city;
        else return _unit;
    }

    /**
     *
     */
    public Unit getUnit() {
        // There's never a unit in a square with a city- there may be units in the city itself..
        if (_city != null) return null;
        else return _unit;
    }

    /**
     *
     */
    public Unit getHiddenUnit() {
        return _hiddenUnit;
    }

    /**
     *
     */
    public City getCity() {
        return _city;
    }

    /**
     *
     */
    public boolean isWater() {
        return false;
    }

    /**
     *
     */
    public boolean isCoastal() {
        MapSquare nextTo;

        if (isWater()) return false;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX(), (int) _location.getY() + 1));
        if (nextTo != null && nextTo.isWater()) return true;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX(), (int) _location.getY() - 1));
        if (nextTo != null && nextTo.isWater()) return true;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX() + 1, (int) _location.getY()));
        if (nextTo != null && nextTo.isWater()) return true;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX() - 1, (int) _location.getY()));
        if (nextTo != null && nextTo.isWater()) return true;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX() + 1, (int) _location.getY() + 1));
        if (nextTo != null && nextTo.isWater()) return true;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX() - 1, (int) _location.getY() + 1));
        if (nextTo != null && nextTo.isWater()) return true;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX() + 1, (int) _location.getY() - 1));
        if (nextTo != null && nextTo.isWater()) return true;

        nextTo = getController().getMap().getSquareAt(new Point((int) _location.getX() - 1, (int) _location.getY() - 1));
        if (nextTo != null && nextTo.isWater()) return true;

        return false;
    }

    /**
     *
     */
    public boolean isVisibleToPlayer(Player player) {
        return !_hiddenFlags[player.getId()];
    }

    /**
     *
     */
    public void unHide(Player player) {
        _hiddenFlags[player.getId()] = false;
    }

    /**
     *
     */
    public void setHidden(boolean hidden) {
        if (_hiddenFlags == null) _hiddenFlags = new boolean[Player.MAX_PLAYERS];

        for (int index = 0; index < _hiddenFlags.length; index++) {
            _hiddenFlags[index] = hidden;
        }
    }

    /**
     *
     */
    public void setLocation(Point location) {
        _location = location;
    }

    /**
     *
     */
    public void setUnit(Unit unit) {
        _unit = unit;

        // Whenever we set the unit to null, check if there is a hidden unit.  If so, the unit that
        // previously occupied this square was "moving over" the hidden unit, which should now be restored.
        if (unit == null && getHiddenUnit() != null) {
            _unit = getHiddenUnit();
            setHiddenUnit(null);
        }
    }

    /**
     *
     */
    public void setCity(City city) {
        _city = city;
    }

    /**
     *
     */
    public void setHiddenUnit(Unit unit) {
        _hiddenUnit = unit;
    }

    /**
     *
     */
    public void highlight() {
        getRenderer().setHighlight(true);
    }

    /**
     *
     */
    public void unHighlight() {
        getRenderer().setHighlight(false);
    }

    /**
     *
     */
    protected GameController getController() {
        return ConquestFactory.getController();
    }
}
