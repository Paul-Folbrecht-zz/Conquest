package com.osi.conquest.domain.impl.unit;


/**
 * @author Paul Folbrecht
 */
public class Infantry extends LandUnit {
    /**
     *
     */
    public Infantry() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends LandUnit.UnitTypeDataImpl {
        public String getName() {
            return "Infantry";
        }

        public int getMaxMovementPoints() {
            return 1;
        }

        public int getMaxHitPoints() {
            return 2;
        }

        public int getTimeToProduce() {
            return 4;
        }

        public int getDamage() {
            return 2;
        }

        public int getTransportWeight() {
            return 1;
        }
    }
}
