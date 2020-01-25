package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.Unit;
import com.osi.conquest.domain.impl.mapsquare.Forest;
import com.osi.conquest.domain.impl.mapsquare.Mountain;
import com.osi.conquest.domain.impl.mapsquare.Water;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public abstract class LandUnit extends UnitImpl {
    private static final long serialVersionUID = 1L;

    /**
     * Land units can attack cities, any unit in a land square, and non-submersible sea units.
     */
    public boolean canAttack(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        City city = square.getCity();
        Unit unit = square.getUnit();

        if (city != null) return (city.getOwner() != getOwner());
        else if (unit != null && unit.getOwner() != getOwner()) {
            if (!square.isWater()) return true;
            else return (unit instanceof SeaUnit && !((SeaUnit) unit).isSubmersible());
        }

        return false;
    }

    /**
     *
     */
    public boolean canMoveTo(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);

        if (!super.canMoveTo(location)) return false;

        if (square.isWater()) {
            // Only time this is legal is if the unit is trying to board transport.
            if (square.getUnit() != null && square.getUnit() instanceof SeaTransport) {
                SeaTransport transport = (SeaTransport) square.getUnit();
                return (transport.canTransport(this) && transport.doesUnitFit(this));
            }

            return false;
        }

        return true;
    }

    /**
     *
     */
    protected double getDefenseFactor(Unit attacker) {
        double factor = getUnitTypeData().getBaseDefenseFactor(attacker.getClass());
        double modifiers = 0;
        MapSquare square = getController().getMap().getSquareAt(_location);

        if (isInTransport(square)) modifiers--;
        if (square.getCity() != null) modifiers++;

        if (getMapSquare() instanceof Forest) modifiers++;
        else if (getMapSquare() instanceof Mountain) modifiers += 2;

        if (modifiers > 2) modifiers = 2;

        if (modifiers < 0) factor = factor * (Math.pow(.5, Math.abs(modifiers)));
        else if (modifiers > 0) factor = factor * (.5 * modifiers + 1);

        return factor;
    }

    /**
     *
     */
    public static abstract class UnitTypeDataImpl extends UnitImpl.UnitTypeDataImpl {
        public int getSightRange(Class unitClass) {
            if (unitClass.equals(Sub.class) || unitClass.equals(Spy.class)) return 0;
            else return 2;
        }

        public int getMaxRange() {
            return -1;
        }

        public boolean canAttackCity() {
            return true;
        }

        public double getBaseAttackFactor(Class unitClass) {
            if (SeaUnit.class.isAssignableFrom(unitClass)) return 5;
            else return 10;
        }

        public int getMovementCost(Class mapSquareType) {
            if (mapSquareType.equals(Water.class)) return MapSquare.ILLEGAL_MOVE;
            else return 1;
        }
    }
}
