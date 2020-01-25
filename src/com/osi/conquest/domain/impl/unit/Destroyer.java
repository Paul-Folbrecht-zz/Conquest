package com.osi.conquest.domain.impl.unit;


/**
 * @author Paul Folbrecht
 */
public class Destroyer extends SeaUnit {
    /**
     *
     */
    public Destroyer() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends SeaUnit.UnitTypeDataImpl {
        public String getName() {
            return "Destroyer";
        }

        public int getMaxMovementPoints() {
            return 3;
        }

        public int getMaxHitPoints() {
            return 6;
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
            return 16;
        }

        public int getDamage() {
            return 2;
        }
    }
}
