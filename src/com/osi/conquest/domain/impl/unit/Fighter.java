package com.osi.conquest.domain.impl.unit;


/**
 * @author Paul Folbrecht
 */
public class Fighter extends AirUnit {
    /**
     *
     */
    public Fighter() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends AirUnit.UnitTypeDataImpl {
        public String getName() {
            return "Fighter";
        }

        public int getMaxMovementPoints() {
            return 6;
        }

        public int getMaxHitPoints() {
            return 2;
        }

        public int getMaxRange() {
            return 18;
        }

        public int getTimeToProduce() {
            return 8;
        }

        public int getDamage() {
            return 2;
        }

        public int getTransportWeight() {
            return 1;
        }
    }
}
