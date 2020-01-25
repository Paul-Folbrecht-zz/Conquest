package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.Unit;
import com.osi.conquest.domain.impl.mapsquare.Water;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public abstract class SeaUnit extends UnitImpl {
    private static final long serialVersionUID = 1L;
    protected static final int CRIPPLED = 32;

    /**
     *
     */
    public boolean isSubmersible() {
        return false;
    }

    /**
     * SeaUnits can attack any unit on a water square, adjacent land units,
     * and units in cities.
     */
    public boolean canAttack(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        City city = square.getCity();
        Unit unit = square.getUnit();

        if (city != null) return (city.getOwner() != getOwner() && city.getFirstDefender() != null);
        else if (unit != null && unit.getOwner() != getOwner()) {
            if (square.isWater() || unit instanceof LandUnit) return true;
            else return false;
        }

        return false;
    }

    /**
     *
     */
    public boolean canMoveTo(Point location) {
        MapSquare square = ConquestFactory.getController().getMap().getSquareAt(location);

        if (!super.canMoveTo(location)) return false;
        else return square.isWater() || (square.getCity() != null && square.getCity().getOwner() == getOwner());
    }

    /**
     * Overridden to handle the CRIPPLED state, when movement is cut in half.
     */
    public int getMaxMovementPoints() {
        if ((_flags & CRIPPLED) != 0) return getUnitTypeData().getMaxMovementPoints() / 2;
        else return getUnitTypeData().getMaxMovementPoints();
    }

    /**
     * Overridden because sea units can become "crippled" when they have lost at least half their
     * hit points.
     */
    public void damage(int damage) {
        super.damage(damage);

        if ((_flags & CRIPPLED) == 0 && getHitPoints() <= (getUnitTypeData().getMaxHitPoints() / 2)) {
            _flags |= CRIPPLED;
            if (getMovementPoints() > getMaxMovementPoints() / 2) _movementPoints = getMaxMovementPoints() / 2;
        }
    }

    /**
     * Overridden to handle taking the unit out of the CRIPPLED state when hit points are restored
     * to above half.
     */
    public void repair(int amount) {
        super.repair(amount);
        if (getHitPoints() > getUnitTypeData().getMaxHitPoints()) _flags &= ~CRIPPLED;
    }

    /**
     *
     */
    public static abstract class UnitTypeDataImpl extends UnitImpl.UnitTypeDataImpl {
        public int getMaxRange() {
            return -1;
        }

        public int getSightRange(Class unitClass) {
            if (unitClass.equals(Sub.class) || unitClass.equals(Spy.class)) return 0;
            else return 2;
        }

        public double getBaseAttackFactor(Class unitClass) {
            if (AirUnit.class.isAssignableFrom(unitClass)) return 5;
            else return 10;
        }

        public int getMovementCost(Class mapSquareType) {
            if (mapSquareType.isAssignableFrom(Water.class)) return 1;
            else return MapSquare.ILLEGAL_MOVE;
        }

        public int getTransportWeight() {
            return 1000;
        }

        public boolean canAttackCity() {
            return false;
        }
    }
}
