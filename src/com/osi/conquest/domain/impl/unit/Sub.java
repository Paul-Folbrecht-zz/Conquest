package com.osi.conquest.domain.impl.unit;


/**
 * @author Paul Folbrecht
 */
public class Sub extends SeaUnit {
    /**
     *
     */
    public Sub() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public boolean isSubmersible() {
        return true;
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends SeaUnit.UnitTypeDataImpl {
        public String getName() {
            return "Sub";
        }

        public int getMaxMovementPoints() {
            return 2;
        }

        public int getMaxHitPoints() {
            return 4;
        }

        public int getMaxSightRange() {
            return 2;
        }

        public int getSightRange(Class unitClass) {
            if (AirUnit.class.isAssignableFrom(unitClass)) {
                return 1;
            } else if (unitClass.equals(Spy.class)) {
                return 0;
            }

            return 2;
        }

        public int getTimeToProduce() {
            return 16;
        }

        public int getDamage() {
            return 6;
        }

        public boolean canAttackCity() {
            return false;
        }

        public double getBaseDefenseFactor(Class unitClass) {
            return 5;
        }
    }
}
