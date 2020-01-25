package com.osi.conquest.domain.impl.unit;


/**
 * @author Paul Folbrecht
 */
public class Battleship extends SeaUnit {
    /**
     *
     */
    public Battleship() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends SeaUnit.UnitTypeDataImpl {
        public String getName() {
            return "Battleship";
        }

        public int getMaxMovementPoints() {
            return 2;
        }

        public int getMaxHitPoints() {
            return 24;
        }

        public int getTimeToProduce() {
            return 40;
        }

        public int getDamage() {
            return 6;
        }
    }
}
