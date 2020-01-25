package com.osi.conquest.domain;


import java.util.List;


/**
 * This is an interface to a unit type that is capable of transporting other
 * units.
 *
 * @author Paul Folbrecht
 */
public interface Transport {
    public boolean canTransport(Unit unit);

    public boolean doesUnitFit(Unit unit);

    public int getMaxUnitWeight();

    public int getUnitCount();

    public int getTransportedUnitWeight();

    public List getUnits();

    public boolean loadUnit(Unit unit);

    public void unloadUnit(Unit unit);

    public Unit getFirstTransportedUnitWithMovement();

    public void awakeUnits();
}
