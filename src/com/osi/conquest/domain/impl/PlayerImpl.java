package com.osi.conquest.domain.impl;


import com.osi.conquest.Logger;
import com.osi.conquest.domain.*;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class PlayerImpl implements Player {
    private static final long serialVersionUID = 2L;
    protected static Color[] _colors = new Color[Player.MAX_PLAYERS];
    protected List _units = new ArrayList();
    protected List _cities = new ArrayList();
    protected List _messages = new ArrayList();
    protected int _id;
    protected int _turn = 0;
    protected HashMap _unitsInProduction = new HashMap();
    protected HashMap _unitsInPlay = new HashMap();
    protected HashMap _unitsKilled = new HashMap();
    protected HashMap _unitsLost = new HashMap();
    //protected boolean[][] _currentlySightedStatus;
    // lastSighted represents the last known sighting for that square, while currentSighted is always what can be seen (by some unit or city)
    // right "now" (the present turn).
    //
    // - For rendering, we want to draw current normally, if present, and last secondarily as mirage
    // - current will be completely reset at the beginning of each turn
    // - There is *no* blanket reset for last
    // - Setting current will always set last as well
    //      - Thus, the only way a mirage disappears is if that square is sighted again
    //      - "Sighted" means taking into account the capabilities of particular friendly units against that unit
    protected OwnableObject[][] _lastSightedObjects;
    protected OwnableObject[][] _currentSightedObjects;
    //protected List _sightedObjects = new ArrayList();
    protected List _movementReports = new ArrayList();
    protected boolean _isLocalToHost;
    protected String _name;
    protected transient boolean _connected;

    /**
     *
     */
    static {
        _colors[0] = Color.red;
        _colors[1] = Color.blue;
        _colors[2] = Color.green;
        _colors[3] = Color.cyan;
        _colors[4] = Color.magenta;
        _colors[5] = Color.black;
    }

    /**
     *
     */
    public static Color getColor(int index) {
        if (index < MAX_PLAYERS) return _colors[index];
        return null;
    }

    /**
     *
     */
    public PlayerImpl(int id, String name, boolean isLocalToHost) {
        _id = id;
        _name = name;
        _isLocalToHost = isLocalToHost;
        _connected = isLocalToHost;
    }

    public void dump() {
        int width = (int) getController().getMap().getSize().getWidth();
        int height = (int) getController().getMap().getSize().getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (_lastSightedObjects[x][y] != null) Logger.info("Last sighted object at " + x + ", " + y + ": " + _lastSightedObjects[x][y]);
                if (_currentSightedObjects[x][y] != null) Logger.info("Current sighted object at " + x + ", " + y + ": " + _currentSightedObjects[x][y]);
            }
        }
    }

    public void init() {
        int width = (int) getController().getMap().getSize().getWidth();
        int height = (int) getController().getMap().getSize().getHeight();
        _lastSightedObjects = new OwnableObject[width][height];
        _currentSightedObjects = new OwnableObject[width][height];
    }

    /**
     *
     */
    public String toString() {
        return getName();
    }

    /**
     *
     */
    public boolean equals(Object other) {
        return (other instanceof Player && ((Player) other).getId() == getId());
    }

    /**
     *
     */
    public void connected() {
        _connected = true;
    }

    /**
     *
     */
    public boolean isConnected() {
        return _connected || isLocalToHost();
    }

    /**
     *
     */
    public String getName() {
        return _name;
    }

    /**
     *
     */
    public Color getColor() {
        return _colors[getId()];
    }

    /**
     *
     */
    public boolean isLocalToHost() {
        return _isLocalToHost;
    }

    /**
     *
     */
    public int getId() {
        return _id;
    }

    /**
     *
     */
    public List getUnits() {
        return _units;
    }

    /**
     *
     */
    public List getCities() {
        return _cities;
    }

    /**
     *
     */
    public int getCurrentTurn() {
        return _turn;
    }

    /**
     *
     */
    public Unit getFirstUnitWithMovement() {
        Iterator it = _units.iterator();

        while (it.hasNext()) {
            Unit unit = (Unit) it.next();
            if (unit.getMovementPoints() > 0 && !unit.isOnSentry()) return unit;
        }

        return null;
    }

    /**
     *
     */
    public int getUnitsInProduction(Class unitClass) {
        return getValueFrom(_unitsInProduction, unitClass);
    }

    /**
     *
     */
    public int getUnitsInPlay(Class unitClass) {
        return getValueFrom(_unitsInPlay, unitClass);
    }

    /**
     *
     */
    public int getUnitsKilled(Class unitClass) {
        return getValueFrom(_unitsKilled, unitClass);
    }

    /**
     *
     */
    public int getUnitsLost(Class unitClass) {
        return getValueFrom(_unitsLost, unitClass);
    }

    /**
     *
     */
    public int getTimeTillNextDone(Class unitClass) {
        Iterator it = _cities.iterator();
        int turns = 1000;

        while (it.hasNext()) {
            City city = (City) it.next();
            if (city.getUnitTypeInProduction().equals(unitClass) && city.getTurnsTillCompletion() < turns) turns = city.getTurnsTillCompletion();
        }

        if (turns == 1000) turns = 0;

        return turns;
    }

    /**
     *
     */
    public void addMessage(String from, String msg) {
        _messages.add(new Message(from, msg));
    }

    /**
     *
     */
    public void updateUnitsInProduction(Class unitClass, int delta) {
        updateValue(_unitsInProduction, unitClass, delta);
    }

    /**
     *
     */
    public void updateUnitsInPlay(Class unitClass, int delta) {
        updateValue(_unitsInPlay, unitClass, delta);
    }

    /**
     *
     */
    public void updateUnitsKilled(Class unitClass, int delta) {
        updateValue(_unitsKilled, unitClass, delta);
    }

    /**
     *
     */
    public void updateUnitsLost(Class unitClass, int delta) {
        updateValue(_unitsLost, unitClass, delta);
    }

    /**
     *
     */
    public void addCity(City city) {
        _cities.add(city);
    }

    /**
     *
     */
    public void removeCity(City city) {
        _cities.remove(city);
    }

    /**
     *
     */
    public void addUnit(Unit unit) {
        _units.add(unit);
    }

    /**
     *
     */
    public void removeUnit(Unit unit) {
        MapSquare square = getController().getMap().getSquareAt(unit.getLocation());

        _units.remove(unit);

        if (unit.isInTransport(square)) {
            Transport transport = (Transport) square.getUnit();
            transport.unloadUnit(unit);
        }

        if (square.getCity() != null) square.getCity().removeUnit(unit);
    }

    /**
     *
     */
    public void doProduction(City city) {
        getController().showCityDialog(this, city, false);
    }

    /**
     *
     */
    public void doProduction() {
        Iterator it = _cities.iterator();

        while (it.hasNext()) {
            City city = (City) it.next();

            if (city.getTurnsTillCompletion() <= 0) unitFinished(city);
            else if (_turn == 1) {
                // Always bring up the City dialog on the first turn- any start cities aren't producing anything.
                city.resetProduction();
                getController().showCityDialog(this, city, false);
            }
        }
    }

    /**
     *
     */
    public void preActivation() {
        int width = (int) getController().getMap().getSize().getWidth();
        int height = (int) getController().getMap().getSize().getHeight();

        _currentSightedObjects = new OwnableObject[width][height];
        updateSightedStatus();
        repairUnits();
        mergeMovementReports();
    }

    /**
     *
     */
    public void onActivate() {
        handleMessages();
        onStartTurn();
    }

    /**
     *
     */
    public void onDeactivate() {
    }

    /**
     *
     */
    public void onStartTurn() {
        boolean scrolled = false;

        _turn++;
        doProduction();
        sortUnits();

        if (_units.size() > 0) {
            if (getController().nextUnit(null)) scrolled = true;
        }

        if (!scrolled) {
            // Scroll to first city by default.
            if (_cities.size() > 0) {
                City city = (City) _cities.iterator().next();
                getController().scrollTo(city.getLocation());
            }
        }
    }

    /**
     *
     */
    public void onEndTurn() {
        Iterator it = _units.iterator();

        while (it.hasNext()) {
            ((Unit) it.next()).resetMovement();
        }

        it = _cities.iterator();
        while (it.hasNext()) {
            ((City) it.next()).decrementProduction();
        }

        _movementReports.clear();
    }

    /**
     *
     */
    public void paintProductionModeSymbols(Graphics2D graphics) {
        Iterator it = _cities.iterator();

        while (it.hasNext()) {
            City city = (City) it.next();
            Unit dummy = city.getProductionRepresentation();

            dummy.getRenderer().render(graphics);
        }
    }

    public void updateCurrentSightedObject(Point location, OwnableObject object) {
        if (object == null) _currentSightedObjects[location.x][location.y] = null;
        else _currentSightedObjects[location.x][location.y] = (OwnableObject) object.copy();
        updateLastSightedObject(location, object);
    }

    public void updateLastSightedObject(Point location, OwnableObject object) {
        if (object == null) _lastSightedObjects[location.x][location.y] = null;
        else _lastSightedObjects[location.x][location.y] = (OwnableObject) object.copy();
    }

    public OwnableObject getCurrentSightedObject(Point location) {
        return _currentSightedObjects[location.x][location.y];
    }

    public OwnableObject getLastSightedObject(Point location) {
        return _lastSightedObjects[location.x][location.y];
    }

    public boolean isObjectSighted(OwnableObject object, Point location) {
        return _currentSightedObjects[location.x][location.y] != null && _currentSightedObjects[location.x][location.y].equals(object);
    }

    /**
     *
     */
    public void addMovementReport(MovementReport report) {
        _movementReports.add(report);
    }

    /**
     *
     */
    public boolean hasMovementReports() {
        return (_movementReports.size() > 0);
    }

    /**
     *
     */
    public void playMovementReports() {
        for (int index = 0; index < _movementReports.size(); index++) {
            Point point = ((MovementReport) _movementReports.get(index)).play();
            MapSquare square = getController().getMap().getSquareAt(point);
            getController().modelChanged(square);
            getController().updateUI(square.getArea());
        }
    }

    /**
     *
     */
    protected void mergeMovementReports() {
        List newList = new ArrayList();
        List handled = new ArrayList();

        for (int index = 0; index < _movementReports.size(); index++) {
            for (int index2 = 0; index2 < _movementReports.size(); index2++) {
                MovementReport one = (MovementReport) _movementReports.get(index);
                MovementReport two = (MovementReport) _movementReports.get(index2);

                if (one != two && one.overlaps(two)) {
                    newList.add(one.merge(two));
                    _movementReports.remove(two);
                    index2--;
                    handled.add(one);
                    handled.add(two);
                }
            }
        }
        for (int index = 0; index < _movementReports.size(); index++) {
            MovementReport report = (MovementReport) _movementReports.get(index);
            if (!handled.contains(report)) newList.add(report);
        }

        _movementReports = newList;
    }

    /**
     * This method repairs all damaged units in a city by one point per turn.
     */
    protected void repairUnits() {
        Iterator cities = _cities.iterator();

        while (cities.hasNext()) {
            ((City) cities.next()).repairUnits();
        }
    }

    /**
     * This method sorts the player's units by location, which will make them be activated in a sensible order.
     */
    protected void sortUnits() {
        class UnitPositionComparator implements Comparator {
            private Point _origin = new Point(0, 0);

            public int compare(Object one, Object two) {
                int rangeOne = getController().getMap().getRange(_origin, ((Unit) one).getLocation());
                int rangeTwo = getController().getMap().getRange(_origin, ((Unit) two).getLocation());
                return (rangeOne - rangeTwo);
            }

            public boolean equals(Object other) {
                return (other instanceof UnitPositionComparator);
            }
        }

        Collections.sort(_units, new UnitPositionComparator());
    }

    /**
     * This method marks as "sighted" everything visible to the player at the start of the turn - everything visible to each unit and city, that is.
     */
    protected void updateSightedStatus() {
        Iterator it = _units.iterator();

        //_currentlySightedStatus = new boolean[width][height];
        //_sightedObjects.clear();

        while (it.hasNext()) {
            ((Unit) it.next()).updateSightedStatus(false);
        }

        it = _cities.iterator();
        while (it.hasNext()) {
            City city = (City) it.next();
            city.getSightRepresentation().updateSightedStatus(false);
        }
    }

    /**
     *
     */
    protected void handleMessages() {
        for (int index = 0; index < _messages.size(); index++) {
            Message message = (Message) _messages.get(index);
            getController().displayPlayerMessage(message._from, message._msg);
        }

        _messages.clear();
    }

    /**
     *
     */
    protected int getValueFrom(HashMap data, Object key) {
        Integer value = (Integer) data.get(key);

        if (value == null) {
            value = new Integer(0);
            data.put(key, value);
        }

        return value.intValue();
    }

    /**
     *
     */
    protected void updateValue(HashMap data, Object key, int delta) {
        int value = getValueFrom(data, key);
        data.put(key, new Integer(value + delta));
    }

    /**
     *
     */
    protected void unitFinished(City city) {
        Unit unit = city.getNewUnit();

        addUnit(unit);
        city.addUnit(unit);
        updateUnitsInPlay(unit.getClass(), 1);
        city.resetProduction();

        if (!city.isProductionContinuous()) getController().showCityDialog(this, city, false);
    }

    /**
     *
     */
    protected GameController getController() {
        return ConquestFactory.getController();
    }

    /**
     *
     */
    protected class Message implements Serializable {
        public String _from;
        public String _msg;

        public Message(String from, String msg) {
            _from = from;
            _msg = msg;
        }
    }
}
