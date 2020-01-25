package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.Unit;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Paul Folbrecht
 */
public class TroopTransport extends SeaTransport {
    public static final int MAX_UNIT_WEIGHT = 8;
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public TroopTransport() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public int getMaxUnitWeight() {
        return MAX_UNIT_WEIGHT;
    }

    /**
     *
     */
    public boolean canTransport(Unit unit) {
        return (unit instanceof LandUnit);
    }

    /**
     * Overridden to handle moving into/out of cities.
     */
    public Unit.MoveToReturn moveOneSquare(Point location) {
        MapSquare oldSquare = getController().getMap().getSquareAt(_location);
        Unit.MoveToReturn result = super.moveOneSquare(location);

        if (result.wasSuccessful()) {
            MapSquare newSquare = getController().getMap().getSquareAt(_location);

            // Transport moving out of city- automatically take sentried units.
            if (oldSquare.getCity() != null) takeUnitsFromCity(oldSquare.getCity());

            // Transport moving into city- unload units.
            if (newSquare.getCity() != null) unloadUnits(newSquare.getCity());
        }

        return result;
    }

    /**
     *
     */
    protected void takeUnitsFromCity(City city) {
        ArrayList list = (ArrayList) ((ArrayList) city.getUnits()).clone();
        Iterator it = list.iterator();

        while (it.hasNext()) {
            Unit unit = (Unit) it.next();

            if (unit instanceof LandUnit && unit.isOnSentry() && doesUnitFit(unit)) {
                loadUnit(unit);
                city.removeUnit(unit);
            }
        }
    }

    /**
     *
     */
    protected void unloadUnits(City city) {
        Iterator it = _transportedUnits.iterator();

        while (it.hasNext()) {
            Unit unit = (Unit) it.next();
            unit.notInTransport();
            city.addUnit(unit);
        }

        _transportedUnits.clear();
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends SeaUnit.UnitTypeDataImpl {
        public String getName() {
            return "Troop Transport";
        }

        public int getMaxMovementPoints() {
            return 2;
        }

        public int getMaxHitPoints() {
            return 6;
        }

        public int getTimeToProduce() {
            return 20;
        }

        public double getBaseAttackFactor(Class unitClass) {
            return 5;
        }

        public double getBaseDefenseFactor(Class unitClass) {
            return 5;
        }

        public int getDamage() {
            return 2;
        }

        public boolean canAttackCity() {
            return false;
        }
    }
}
