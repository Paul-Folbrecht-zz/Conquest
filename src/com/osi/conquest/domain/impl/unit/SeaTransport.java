package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.domain.Transport;
import com.osi.conquest.domain.Unit;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public abstract class SeaTransport extends SeaUnit implements Transport {
    private static final long serialVersionUID = 1L;
    protected List _transportedUnits = new ArrayList();


    /**
     *
     */
    public SeaTransport() {
    }

    /**
     *
     */
    public abstract boolean canTransport(Unit unit);

    /**
     *
     */
    public abstract int getMaxUnitWeight();

    /**
     *
     */
    public List getUnits() {
        return _transportedUnits;
    }

    public void destroy() {
        super.destroy();
        _transportedUnits.forEach(unit -> ((Unit) unit).destroy());
    }

    /**
     * Overridden to make a deep copy of transported units.
     */
    public Object copy() {
        SeaTransport clone = (SeaTransport) super.copy();
        clone._transportedUnits = (ArrayList) ((ArrayList) _transportedUnits).clone();
        return clone;
    }

    /**
     *
     */
    public int getUnitCount() {
        return _transportedUnits.size();
    }

    /**
     *
     */
    public int getTransportedUnitWeight() {
        Iterator it = _transportedUnits.iterator();
        int weight = 0;

        while (it.hasNext()) {
            weight += ((Unit) it.next()).getUnitTypeData().getTransportWeight();
        }

        return weight;
    }

    /**
     *
     */
    public boolean doesUnitFit(Unit unit) {
        return (getTransportedUnitWeight() + unit.getUnitTypeData().getTransportWeight() <= getMaxUnitWeight());
    }

    /**
     * Overridden to move transported units too.
     */
    public Unit.MoveToReturn moveOneSquare(Point location) {
        Unit.MoveToReturn result = super.moveOneSquare(location);

        if (result.wasSuccessful()) {
            Iterator it = _transportedUnits.iterator();

            while (it.hasNext()) {
                ((Unit) it.next()).setLocation(_location);
            }
        }

        return result;
    }

    /**
     *
     */
    public String getStatusText() {
        StringBuffer buffer = new StringBuffer(super.getStatusText());

        buffer.append("; ");
        buffer.append("" + getUnitCount());
        buffer.append(" unit(s) aboard.");

        return buffer.toString();
    }

    /**
     *
     */
    public Unit getFirstTransportedUnitWithMovement() {
        Iterator it = _transportedUnits.iterator();

        while (it.hasNext()) {
            Unit unit = (Unit) it.next();
            if (unit.getMovementPoints() > 0) return unit;
        }

        return null;
    }

    /**
     *
     */
    public void awakeUnits() {
        Iterator it = _transportedUnits.iterator();

        while (it.hasNext()) {
            Unit unit = (Unit) it.next();
            unit.clearOrders();
        }
    }

    /**
     *
     */
    public boolean loadUnit(Unit unit) {
        if (_transportedUnits.contains(unit))
            throw new RuntimeException("Tried to load a unit onto a transport that is already aboard.");

        if (!doesUnitFit(unit)) return false;

        _transportedUnits.add(unit);
        unit.sentry();
        unit.setLocation(_location);
        unit.inTransport();

        return true;
    }

    /**
     *
     */
    public void unloadUnit(Unit unit) {
        if (!_transportedUnits.contains(unit))
            throw new RuntimeException("Tried to remove a unit from transport that is not aboard.");

        _transportedUnits.remove(unit);
        unit.notInTransport();
    }
}
