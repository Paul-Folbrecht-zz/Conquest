package com.osi.conquest.domain.impl;


import com.osi.conquest.domain.*;
import com.osi.conquest.domain.impl.unit.UnitImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class CityImpl implements City {
    private static final long serialVersionUID = 1L;
    protected List _units = new ArrayList();
    protected int _turnsTillCompletion;
    protected UnitPath _autoForwardPath;
    protected Player _owner;
    protected Point _location;
    protected boolean _continuousProduction = false;
    protected String _name;
    protected Unit _sightRepresentation;
    protected Unit _productionRepresentation;
    protected transient City.Renderer _renderer;

    /**
     *
     */
    public CityImpl(Point location, String name) {
        _location = location;
        _name = name;
        _sightRepresentation = ConquestFactory.createUnit("com.osi.conquest.domain.impl.unit.Infantry", _owner, getLocation());
        _productionRepresentation = ConquestFactory.createUnit("com.osi.conquest.domain.impl.unit.Infantry", _owner, getLocation());
    }

    /**
     *
     */
    public Object copy() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public City.Renderer getRenderer() {
        if (_renderer == null) _renderer = ConquestFactory.createCityRenderer(this);
        return _renderer;
    }

    /**
     *
     */
    public String getName() {
        return _name;
    }

    /**
     *
     */
    public List getUnits() {
        return _units;
    }

    /**
     *
     */
    public boolean isUnitPresent(Unit unit) {
        return (_units.indexOf(unit) != -1);
    }

    /**
     *
     */
    public boolean isWithinSightRange(OwnableObject object) {
        return getSightRepresentation().isWithinSightRange(object);
    }

    /**
     *
     */
    public Player getOwner() {
        return _owner;
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
    public Class getUnitTypeInProduction() {
        return _productionRepresentation.getClass();
    }

    /**
     *
     */
    public int getTurnsTillCompletion() {
        return _turnsTillCompletion;
    }

    /**
     *
     */
    public int getTurnsTillCompletion(Class unitClass) {
        if (unitClass.equals(_productionRepresentation.getClass())) {
            return _turnsTillCompletion;
        } else {
            return UnitImpl.getTypeData(unitClass).getTimeToProduce();
        }
    }

    /**
     *
     */
    public Unit getProductionRepresentation() {
        return _productionRepresentation;
    }

    /**
     *
     */
    public Unit getNewUnit() {
        Unit unit = ConquestFactory.createUnit(_productionRepresentation.getClass().getName(), _owner, getLocation());

        try {
            if (getAutoForwardPath() != null) unit.setMovePath((UnitPath) getAutoForwardPath().clone());
        } catch (CloneNotSupportedException e) {
        }

        return unit;
    }

    /**
     *
     */
    public boolean isProductionContinuous() {
        return _continuousProduction;
    }

    /**
     *
     */
    public UnitPath getAutoForwardPath() {
        return _autoForwardPath;
    }

    /**
     *
     */
    public void addUnit(Unit unit) {
        _units.add(unit);
    }

    /**
     *
     */
    public void removeUnit(Unit unit) {
        _units.remove(unit);
    }

    /**
     *
     */
    public void setOwner(Player owner) {
        _owner = owner;
        _sightRepresentation = ConquestFactory.createUnit("com.osi.conquest.domain.impl.unit.Infantry", _owner, getLocation());
        setProduction(com.osi.conquest.domain.impl.unit.Infantry.class);
        resetProduction();

        if (owner != null) {
            owner.updateUnitsInProduction(_productionRepresentation.getClass(), 1);
        }

        // When a city changes hands any units inside it are done for.  Note that the only units
        // that could possibly be left are ones not capable of defending the city.
        for (int index = 0; index < _units.size(); index++) {
            ((Unit) _units.get(index)).destroy();
        }
    }

    /**
     *
     */
    public void decrementProduction() {
        _turnsTillCompletion--;
    }

    /**
     *
     */
    public void resetProduction() {
        _turnsTillCompletion = _productionRepresentation.getUnitTypeData().getTimeToProduce();
    }

    /**
     *
     */
    public void setProduction(Class unitType) {
        if (getOwner() != null) {
            if (_productionRepresentation != null) getOwner().updateUnitsInProduction(_productionRepresentation.getClass(), -1);
            getOwner().updateUnitsInProduction(unitType, 1);
        }

        _productionRepresentation = ConquestFactory.createUnit(unitType.getName(), _owner, getLocation());
        resetProduction();
        setAutoForwardPath(null);
    }

    /**
     *
     */
    public void setContinuousProduction(boolean continuous) {
        _continuousProduction = continuous;
    }

    /**
     *
     */
    public void setAutoForwardPath(UnitPath path) {
        _autoForwardPath = path;
        _productionRepresentation.setMovePath(path);
        setContinuousProduction(path != null);
    }

    /**
     *
     */
    public Unit getSightRepresentation() {
        return _sightRepresentation;
    }

    /**
     *
     */
    public Unit getFirstDefender() {
        for (int index = 0; index < _units.size(); index++) {
            Unit unit = (Unit) _units.get(index);
            if (unit.getUnitTypeData().canAttackCity()) return unit;
        }

        return null;
    }

    /**
     *
     */
    public Color getDisplayableColor() {
        Player player = getController().getCurrentPlayer();
        Color color = Color.white;

        // The displayed owner of the city is determined as follows: if the owner is the current player, this is always displayed.  If not, the current
        // player's last sighting of the city (which may have been never) is used instead.
        if (getOwner() == player) color = player.getColor();
        else {
            OwnableObject city = player.getLastSightedObject(getLocation());
            if (city != null && city.getOwner() != null) color = city.getOwner().getColor();
        }

        return color;
    }

    /**
     *
     */
    public void repairUnits() {
        for (int index = 0; index < _units.size(); index++) {
            ((Unit) _units.get(index)).repair(1);
        }
    }

    /**
     *
     */
    protected GameController getController() {
        return ConquestFactory.getController();
    }
}
