package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.Unit;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class Spy extends LandUnit {
    /**
     *
     */
    public Spy() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     * Overridden because spies can't attack anything.
     */
    public boolean canAttack(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        Unit unit = square.getUnit();

        if (unit != null && unit.getOwner() != getOwner() && unit instanceof Spy) return true;
        else return false;
    }

    /**
     * Overridden because spies can move onto enemy cities.
     */
    public boolean canMoveTo(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        City city = square.getCity();

        if (city != null && city.getOwner() != null) return true;

        return super.canMoveTo(location);
    }

    /**
     * Overridden to support spies investigating enemy cities.  Doing so causes the city's
     * production dialog to be shown in read-only mode.  The unit does not actually move into
     * the city.
     */
    public Unit.MoveToReturn moveOneSquare(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        City city = square.getCity();
        Unit.MoveToReturn result;

        if (city != null && city.getOwner() != null && city.getOwner() != getOwner()) {
            getController().showCityDialog(city.getOwner(), city, true);
            decrementMovementPoints(1);

            if (getRandom() < .5) {
                getController().sendPlayerMessage("Host", getController().getCurrentPlayer(), "Your Spy was captured.");
                getController().sendPlayerMessage("Host", city.getOwner(), "An enemy Spy was captured in " + city.getName());
                destroy();
            } else if (getRandom() < .5) {
                getController().sendPlayerMessage("Host", city.getOwner(), city.getName() + " was investigated by an enemy Spy!");
            }

            result = new UnitImpl.MoveToReturnImpl(true, Unit.MoveToReturn.IGNORE_THIS);
        } else {
            result = super.moveOneSquare(location);
        }

        return result;
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends LandUnit.UnitTypeDataImpl {
        public String getName() {
            return "Spy";
        }

        public int getMaxMovementPoints() {
            return 2;
        }

        public int getMaxHitPoints() {
            return 1;
        }

        public int getTimeToProduce() {
            return 12;
        }

        public int getDamage() {
            return 1;
        }

        public int getTransportWeight() {
            // Spies are very light.
            return 0;
        }

        public double getBaseAttackFactor(Class unitClass) {
            return 1;
        }

        public double getBaseDefenseFactor(Class unitClass) {
            return 1;
        }
    }
}
