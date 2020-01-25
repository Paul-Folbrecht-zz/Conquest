package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.domain.impl.mapsquare.Forest;
import com.osi.conquest.domain.impl.mapsquare.Mountain;
import com.osi.conquest.domain.impl.mapsquare.Water;


/**
 * @author Paul Folbrecht
 */
public class Armor extends LandUnit {
    /**
     *
     */
    public Armor() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends LandUnit.UnitTypeDataImpl {
        public String getName() {
            return "Armor";
        }

        public int getMaxMovementPoints() {
            return 2;
        }

        public int getMaxHitPoints() {
            return 4;
        }

        public int getTimeToProduce() {
            return 8;
        }

        public int getDamage() {
            return 2;
        }

        public int getTransportWeight() {
            return 2;
        }

        public int getMovementCost(Class mapSquareType) {
            if (mapSquareType.equals(Water.class)) {
                return MapSquare.ILLEGAL_MOVE;
            } else if (mapSquareType.equals(Mountain.class) || mapSquareType.equals(Forest.class)) {
                return 2;
            } else {
                return 1;
            }
        }
    }
}
