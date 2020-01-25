package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.Unit;

import java.awt.*;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class Bomber extends AirUnit {
    /**
     *
     */
    public Bomber() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public boolean canAttack(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        City city = square.getCity();
        Unit unit = square.getUnit();

        if (city != null) {
            return (city.getOwner() != getOwner());
        } else {
            return (unit != null && unit.getOwner() != getOwner());
        }
    }

    /**
     *
     */
    public boolean attack(City city) {
        // Each time a city is attacked a new Infantry unit is constructed to represent it in combat.
        boolean result = attack(ConquestFactory.createUnit("com.osi.conquest.domain.impl.unit.Infantry", city.getOwner(), city.getLocation()));

        if (result) {
            // When a bomber defeats a city, a random unit, or the city's production, is destroyed.
            List units = city.getUnits();
            int roll = (int) (Math.random() * 1000) % (units.size() + 1);

            if (roll == 0) {
                city.resetProduction();
                getController().sendPlayerMessage("Host", getOwner(), "You destroyed the city's production.");
            } else {
                Unit unit = (Unit) units.get(roll - 1);
                unit.destroy();
                getController().sendPlayerMessage("Host", getOwner(), "You destroyed a(n) " + unit.getUnitTypeData().getName() + ".");
            }
        }

        return result;
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends AirUnit.UnitTypeDataImpl {
        public String getName() {
            return "Bomber";
        }

        public int getMaxMovementPoints() {
            return 4;
        }

        public int getMaxHitPoints() {
            return 2;
        }

        public int getMaxRange() {
            return 24;
        }

        public int getTimeToProduce() {
            return 8;
        }

        public int getDamage() {
            return 4;
        }

        public double getBaseDefenseFactor(Class unitClass) {
            if (unitClass.equals(Fighter.class)) {
                return 5;
            }

            return 10;
        }

        public int getTransportWeight() {
            return 2;
        }
    }
}
