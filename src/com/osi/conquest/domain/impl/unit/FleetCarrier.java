package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.Unit;


/**
 * @author Paul Folbrecht
 */
public class FleetCarrier extends SeaTransport {
    /**
     *
     */
    public FleetCarrier() {
        _unitTypeData = new UnitTypeDataImpl();
    }

    /**
     *
     */
    public boolean canTransport(Unit unit) {
        return (unit instanceof AirUnit);
    }

    /**
     *
     */
    public int getMaxUnitWeight() {
        return 12;
    }

    /**
     *
     */
    public static class UnitTypeDataImpl extends SeaUnit.UnitTypeDataImpl {
        public String getName() {
            return "Carrier";
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
            return 2;
        }

        public double getBaseAttackFactor(Class unitClass) {
            return 5;
        }
    }
}
