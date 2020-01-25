package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.Player;
import com.osi.conquest.domain.Unit;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public abstract class AirUnit extends UnitImpl {
    private static final long serialVersionUID = 1L;
    protected int _range;

    /**
     *
     */
    public void init(Player player, Point startLoc) {
        super.init(player, startLoc);
        resetRange();
    }

    /**
     *
     */
    public int getRange() {
        return _range;
    }

    /**
     *
     */
    public void setRange(int range) {
        _range = range;
    }

    /**
     *
     */
    public void resetRange() {
        _range = getUnitTypeData().getMaxRange();
    }

    /**
     *
     */
    public Unit.MoveToReturn moveOneSquare(Point location) {
        Unit.MoveToReturn result = super.moveOneSquare(location);

        if (result.wasSuccessful()) {
            MapSquare square = getController().getMap().getSquareAt(_location);

            setRange(getRange() - 1);
            if (getRange() < 0) return outOfGas();

            // Moving into city or carrier?
            if (square.getCity() != null || isInTransport(square)) {
                setMovementPoints(0);
                setRange(getUnitTypeData().getMaxRange());
            }
        }

        return result;
    }

    /**
     *
     */
    Unit.MoveToReturn outOfGas() {
        getController().onUnitOutOfGas(this);
        getController().deselectUnit();
        destroy();
        getController().updateUI(getArea());

        return new UnitImpl.MoveToReturnImpl(false, Unit.MoveToReturn.ERROR_OUTOFGAS);
    }

    /**
     *
     */
    public String getStatusText() {
        StringBuffer buffer = new StringBuffer(super.getStatusText());

        buffer.append("; Range ");
        buffer.append("" + _range);
        buffer.append("/");
        buffer.append("" + getUnitTypeData().getMaxRange());

        return buffer.toString();
    }

    /**
     *
     */
    public String[][] getDisplayableInfo() {
        String[][] data = new String[6][2];
        String[][] unitData = super.getDisplayableInfo();

        for (int row = 0; row < unitData.length; row++) {
            data[row] = unitData[row];
        }

        data[5][0] = "Range";
        data[5][1] = "" + getRange() + "/" + getUnitTypeData().getMaxRange();

        return data;
    }

    /**
     * Air units can attack any unit as well as units in cities.
     */
    public boolean canAttack(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        City city = square.getCity();
        Unit unit = square.getUnit();

        if (city != null) return (city.getOwner() != getOwner() && city.getFirstDefender() != null);
        else return (unit != null && unit.getOwner() != getOwner());
    }

    /**
     * Overridden to allow air units to move over friendly units even when not on move-to.
     */
    protected boolean checkOverlappingMove(MapSquare square) {
        boolean result = super.checkOverlappingMove(square);

        if (!result) result = (getController().getMap().findUnoccupiedSquareWithinRadius(this, getMovementPoints() - 1) != null);

        return result;
    }

    /**
     *
     */
    public static abstract class UnitTypeDataImpl extends UnitImpl.UnitTypeDataImpl {
        public boolean canAttackCity() {
            return false;
        }

        public int getSightRange(Class unitClass) {
            if (unitClass.equals(Sub.class) || unitClass.equals(Spy.class)) return 0;
            else return 2;
        }

        public int getMovementCost(Class mapSquareType) {
            return 1;
        }

        public int getTransportWeight() {
            return 1;
        }
    }
}
