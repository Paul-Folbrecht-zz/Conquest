package com.osi.conquest.domain.impl.unit;


/**
 * @author Paul Folbrecht
 */
public class Cruiser extends SeaUnit {
    /**
     *
     */
    public Cruiser() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends SeaUnit.UnitTypeDataImpl {
        public String getName() {
            return "Cruiser";
        }

        public int getMaxMovementPoints() {
            return 2;
        }

        public int getMaxHitPoints() {
            return 16;
        }

        public int getSightRange(Class unitClass) {
            if (unitClass.equals(Spy.class)) {
                return 0;
            } else if (unitClass.equals(Sub.class)) {
                return 1;
            }

            return 2;
        }

        public int getTimeToProduce() {
            return 24;
        }

        public int getDamage() {
            return 4;
        }
    }
}
