package com.osi.conquest.domain.impl.unit;


import com.osi.conquest.Logger;
import com.osi.conquest.domain.*;
import com.osi.conquest.domain.event.UnitActionListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public abstract class UnitImpl implements Unit {
    private static final long serialVersionUID = 2L;
    protected static final int ON_SENTRY = 1;
    protected static final int FACING_RIGHT = 2;
    protected static final int IN_TRANSPORT = 4;
    protected static final int IS_ENTRENCHED = 8;
    protected static final int ON_PATROL = 16;

    protected static List _classes = new ArrayList();
    protected static HashMap _instances = new HashMap();
    protected static int _lastId = 1;

    protected Unit.UnitTypeData _unitTypeData;
    protected Player _owner;
    protected Point _location;
    protected UnitPath _path;
    protected int _hitPoints;
    protected int _movementPoints;
    protected long _flags;
    protected int _id;
    protected boolean _dead = false;
    protected List _listeners = new ArrayList();
    protected transient Unit.Renderer _renderer;

    /**
     *
     */
    static {
        _classes.add(Infantry.class);
        _classes.add(Armor.class);
        _classes.add(Spy.class);
        _classes.add(Fighter.class);
        _classes.add(Bomber.class);
        _classes.add(TroopTransport.class);
        _classes.add(Sub.class);
        _classes.add(Destroyer.class);
        _classes.add(Cruiser.class);
        _classes.add(Battleship.class);
        _classes.add(FleetCarrier.class);

        createInstances();
    }

    /**
     *
     */
    public static List getClasses() {
        return _classes;
    }

    /**
     *
     */
    public static Unit.UnitTypeData getTypeData(Class unitClass) {
        return ((Unit) _instances.get(unitClass)).getUnitTypeData();
    }

    /**
     *
     */
    public UnitImpl() {
        // Force loading of static data.
        getRenderer();
    }

    /**
     *
     */
    public void init(Player player, Point startLoc) {
        _owner = player;
        _location = startLoc;
        resetDamage();
        resetMovement();
        _id = _lastId++;
    }

    /**
     *
     */
    public boolean equals(Object object) {
        return (object instanceof Unit && ((UnitImpl) object).getId() == getId());
    }

    /**
     *
     */
    public Unit.Renderer getRenderer() {
        if (_renderer == null) _renderer = ConquestFactory.createUnitRenderer(this);
        return _renderer;
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
    public Player getOwner() {
        return _owner;
    }

    /**
     *
     */
    public Unit.UnitTypeData getUnitTypeData() {
        return _unitTypeData;
    }

    /**
     *
     */
    public String[][] getDisplayableInfo() {
        String[][] data = new String[5][2];
        String[] labels = {"Type:", "Id:", "Hit Points:", "Movement:", "Orders:"};
        Unit.UnitTypeData typeData = getUnitTypeData();

        for (int row = 0; row < data.length; row++) {
            data[row] = new String[2];
            data[row][0] = labels[row];
        }

        data[0][1] = typeData.getName();
        data[1][1] = "" + getId();
        data[2][1] = "" + getHitPoints() + "/" + typeData.getMaxHitPoints();
        data[3][1] = "" + getMovementPoints() + "/" + getMaxMovementPoints();
        data[4][1] = getOrders();

        return data;
    }

    /**
     *
     */
    public int getMaxMovementPoints() {
        return getUnitTypeData().getMaxMovementPoints();
    }

    /**
     *
     */
    public boolean isOnSentry() {
        return ((_flags & ON_SENTRY) != 0);
    }

    /**
     *
     */
    public boolean isFacingRight() {
        return ((_flags & FACING_RIGHT) != 0);
    }

    /**
     *
     */
    public Point getLocation() {
        return _location;
    }

    /**
     *
     */
    public MapSquare getMapSquare() {
        return getController().getMap().getSquareAt(getLocation());
    }

    /**
     *
     */
    public Rectangle getArea() {
        return getMapSquare().getArea();
    }

    /**
     *
     */
    public Rectangle getSightArea() {
        int range = getUnitTypeData().getMaxSightRange();
        return new Rectangle((_location.x - range), (_location.y - range), range * 2 + 1, range * 2 + 1);
    }

    /**
     *
     */
    public String toString() {
        return getStatusText();
    }

    /**
     *
     */
    public String getOrders() {
        if (isOnSentry()) return "Sentried";
        else if (isOnPatrol()) return "Patrolling";
        else if (hasMoveTo()) return "Move To";
        else return "No Orders";
    }

    /**
     *
     */
    public String getStatusText() {
        StringBuffer buffer = new StringBuffer(200);
        Unit.UnitTypeData typeData = getUnitTypeData();

        buffer.append(typeData.getName());
        buffer.append("[Id " + _id + "]");
        buffer.append(": HP ");
        buffer.append(_hitPoints);
        buffer.append("/");
        buffer.append(typeData.getMaxHitPoints());
        buffer.append("; MP ");
        buffer.append(_movementPoints);
        buffer.append("/");
        buffer.append(typeData.getMaxMovementPoints());
        buffer.append(" [");
        buffer.append(getOrders());
        buffer.append("]");

        return buffer.toString();
    }

    /**
     *
     */
    public int getUnitCount() {
        return 0;
    }

    /**
     *
     */
    public int getRange() {
        return 0;
    }

    /**
     *
     */
    public int getHitPoints() {
        return _hitPoints;
    }

    /**
     *
     */
    public int getMovementPoints() {
        return _movementPoints;
    }

    /**
     *
     */
    public int getRangeTo(Point location) {
        return (int) Math.max(Math.abs(_location.getX() - location.getX()), Math.abs(_location.getY() - location.getY()));
    }

    /**
     *
     */
    public boolean isAdjacentTo(Point location) {
        return (getRangeTo(location) <= 1);
    }

    /**
     *
     */
    public boolean isWithinSightRange(OwnableObject object) {
        return (getRangeTo(object.getLocation()) <= getUnitTypeData().getSightRange(object.getClass()));
    }

    /**
     *
     */
    public boolean hasMoveTo() {
        return (_path != null);
    }

    /**
     *
     */
    public UnitPath getPath() {
        return _path;
    }

    /**
     *
     */
    public boolean isOnPatrol() {
        return ((_flags & ON_PATROL) != 0);
    }

    /**
     *
     */
    public boolean isInTransport(MapSquare square) {
        return square.getUnit() != null && square.getUnit() != this && square.getUnit().getOwner() == getOwner() && square.getUnit() instanceof Transport;
    }

    /**
     *
     */
    public void resetMovement() {
        _movementPoints = getMaxMovementPoints();
    }

    /**
     *
     */
    public void resetDamage() {
        _hitPoints = getUnitTypeData().getMaxHitPoints();
    }

    /**
     *
     */
    public void setMovementPoints(int points) {
        _movementPoints = points;
        if (_movementPoints < 0) _movementPoints = 0;
    }

    /**
     *
     */
    public void decrementMovementPoints(int points) {
        _movementPoints -= points;

        if (_movementPoints < 0) {
            _movementPoints = 0;
        }
    }

    /**
     *
     */
    public void setLocation(Point location) {
        _location = new Point(location);
    }

    /**
     *
     */
    public void setMovePath(UnitPath path) {
        _path = path;
    }

    /**
     *
     */
    public void clearMoveTo() {
        _path = null;
    }

    /**
     *
     */
    public void onPatrol() {
        _flags = (_flags | ON_PATROL);
    }

    /**
     *
     */
    public void offPatrol() {
        _flags = (_flags & ~ON_PATROL);
        clearMoveTo();
    }

    /**
     *
     */
    public void inTransport() {
        _flags = (_flags | IN_TRANSPORT);
    }

    /**
     *
     */
    public void notInTransport() {
        _flags = (_flags & ~IN_TRANSPORT);
        unSentry();
    }

    /**
     *
     */
    public void clearOrders() {
        offPatrol();
        unSentry();
        setMovePath(null);
    }

    /**
     *
     */
    public void sentry() {
        clearOrders();
        _flags = (_flags | ON_SENTRY);
        setMovementPoints(0);
    }

    /**
     *
     */
    public void unSentry() {
        _flags = (_flags & ~ON_SENTRY);
    }

    /**
     *
     */
    public boolean select() {
        clearOrders();
        getController().scrollTo(_location);

        return true;
    }

    /**
     *
     */
    public boolean deselect() {
        if (!getController().getSelectedUnit().equals(this)) {
            return false;
        }

        getController().modelChanged(getMapSquare());

        return true;
    }

    /**
     *
     */
    public Object copy() {
        Object clone;
        try {
            clone = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        // Don't copy the move path- this method is used only for historical sighting data.
        ((Unit) clone).setMovePath(null);
        // Don't copy renderer- it keeps a reference to original unit.
        ((UnitImpl) clone)._renderer = null;

        return clone;
    }

    /**
     * This method updates the player's knowledge of the terrain within this unit's sight range, including, of course, enemy units and cities within
     * that range.  It should be called during player initialization and whenever the unit is moved.
     *
     * @return true if a new unit is sighted.  This is the case if a square was previously unsighted and contains an enemy unit.
     */
    public boolean updateSightedStatus(boolean updateUI) {
        int sightRange = getUnitTypeData().getMaxSightRange();
        boolean foundUnit = false;

        for (int row = (int) _location.getX() - sightRange; row <= _location.getX() + sightRange; row++) {
            for (int col = (int) _location.getY() - sightRange; col <= _location.getY() + sightRange; col++) {
                Point point = new Point(row, col);
                MapSquare square = getController().getMap().getSquareAt(point);

                if (square != null) {
                    square.unHide(getOwner());

                    OwnableObject object = null;
                    if (square.getCity() != null) object = square.getCity();
                    else if (square.getUnit() != null && square.getUnit().getOwner() != getOwner()) {
                        if (isWithinSightRange(square.getUnit())) {
                            object = square.getUnit();
                            if (!getOwner().isObjectSighted(square.getUnit(), point)) foundUnit = true;
                        }
                    }

                    if (object != null) {
                        getOwner().updateCurrentSightedObject(square.getLocation(), object);
                        if (updateUI) getController().modelChanged(square);
                    } else {
                        // If there is a mirage at this square, but no actual unit, we must determine if it stays or not.  It does as long as
                        // there does not exist a unit that can see that square and the mirage is within that sight range.  In other words,
                        // if units of the mirage type in that square are "revealed" to any enemy unit, the mirage disappears, because the player
                        // would know if there's really such a unit there or not.
                        OwnableObject mirage = getOwner().getLastSightedObject(point);
                        if (mirage != null && isWithinSightRange(mirage)) getOwner().updateLastSightedObject(point, null);
                        if (updateUI) getController().modelChanged(square);
                    }
                }
            }
        }

        return foundUnit;
    }

    /**
     * This method determines whether the unit can move to the square at the given location.  It takes into account more than just the terrain type-
     * it's also illegal to move to a square containing an enemy unit or city.
     *
     * See also Unit.checkMove()
     *
     * @return true if the move is legal, false otherwise.
     */
    public boolean canMoveTo(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);

        if (square.getCity() != null) return (square.getCity().getOwner() == _owner);
        else return true;
    }

    /**
     *
     */
    public Unit.MoveToReturn moveOneSquare(Point location) {
        Unit.MoveToReturn result = checkMove(location);
        MapSquare oldSquare = getMapSquare();
        MapSquare newSquare = getController().getMap().getSquareAt(location);
        Rectangle oldArea = getSightArea();
        boolean enteredTransport;

        if (!result.wasSuccessful()) {
            Logger.info("Movement failed: " + result.getDetail());
            return result;
        }
        getController().setGameDirty();

        if (oldSquare.getUnit() == this) oldSquare.setUnit(null);

        setFacing(location, _location);
        _location = location;

        if (newSquare.getCity() != null) {
            if (newSquare.getCity().getOwner().equals(_owner)) newSquare.getCity().addUnit(this);
        }

        checkForLeavingTransport(oldSquare.getUnit());
        enteredTransport = checkForEnteringTransport(newSquare.getUnit());

        if (newSquare.getUnit() != null && !enteredTransport) newSquare.setHiddenUnit(newSquare.getUnit());

        if (oldSquare.getCity() != null) oldSquare.getCity().removeUnit(this);

        if (!enteredTransport) {
            decrementMovementPoints(getUnitTypeData().getMovementCost(newSquare.getClass()));
            if (newSquare.getCity() == null) newSquare.setUnit(this);
        }

        if (updateSightedStatus(true)) result = new MoveToReturnImpl(true, Unit.MoveToReturn.FOUND_ENEMY);

        getController().modelChanged(oldSquare);
        getController().modelChanged(newSquare);
        getController().updateUI(oldArea.union(getSightArea()));

        broadcastMove(oldSquare.getLocation(), newSquare.getLocation());

        return result;
    }

    /**
     * @return true if the unit expended all its movement points successfully, false if not.  (The unit will stop if it can't move, finishes its path,
     * or sights a new enemy unit.)
     */
    public boolean executeMoveTo() {
        boolean done = false;

        while (!done) {
            MapSquare nextSquare;

            nextSquare = _path.getNextSquare();
            if (nextSquare == null) {
                if (isOnPatrol()) {
                    // If the unit is patrolling, path wraps back to the beginning.
                    _path.reset();
                    nextSquare = _path.getNextSquare();
                } else {
                    // Path is complete - unit still has movement points.
                    done = true;
                }
            }

            if (!done) {
                Unit.MoveToReturn result = moveOneSquare(nextSquare.getLocation());

                if (result.getDetail() == Unit.MoveToReturn.FAILED_FRIENDLY_UNIT) {
                    // Square has another friendly unit.  If this unit has a Move To order, we can let it move, then continue moving this unit.
                    if (nextSquare.getUnit().hasMoveTo() && nextSquare.getUnit().getMovementPoints() > 0) {
                        nextSquare.getUnit().executeMoveTo();
                        result = moveOneSquare(nextSquare.getLocation());
                    }
                }

                if (result.getDetail() == Unit.MoveToReturn.FOUND_ENEMY) done = true;
                else if (!result.wasSuccessful()) {
                    // Need to retry the move next time, so the path has to be rolled back one square.
                    if (result.getDetail() == Unit.MoveToReturn.OUT_OF_MOVEMENT) _path.moveToPreviousSquare();
                    done = true;
                }

                if (getMovementPoints() <= 0) done = true;
            }
        }

        if (getMovementPoints() > 0) getController().setSelectedUnit(this);
        else getController().setSelectedUnit(null);

        return (getMovementPoints() <= 0);
    }

    /**
     *
     */
    public abstract boolean canAttack(Point location);

    /**
     *
     */
    public boolean canIntentionallyAttack(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);
        Unit unit = square.getUnit();

        if (unit != null && !isWithinSightRange(unit)) return false;
        else return canAttack(location);
    }

    /**
     *
     */
    public boolean attack(Unit defender) {
        boolean result;

        // This check is made because if unit A cannot see another unit B it can never willfully
        // "attack" it; in effect A bumps into B which then attacks.  This is most relevant in the
        // case of subs run into by unsuspecting transports or other subsurface-blind sea units.
        if (getUnitTypeData().getSightRange(defender.getClass()) == 0) {
            if (defender.getUnitTypeData().getSightRange(getClass()) != 0) return !(defender.attack(this));
        }

        result = attackImpl((UnitImpl) defender);
        broadcastCombat(defender, result);

        return result;
    }

    /**
     *
     */
    public boolean attack(City city) {
        if (city.getFirstDefender() != null) {
            boolean result = attack(city.getFirstDefender());
            if (result) _movementPoints--;
            return result;
        } else {
            // Each time a city is attacked a new Infantry unit is constructed to represent it in combat.
            boolean result = attack(ConquestFactory.createUnit("com.osi.conquest.domain.impl.unit.Infantry", city.getOwner(), city.getLocation()));

            if (result) {
                Player loser = city.getOwner();

                // We now own the city.
                city.setOwner(getOwner());
                getOwner().addCity(city);
                getOwner().doProduction(city);

                if (loser != null) {
                    loser.removeCity(city);
                    getController().checkForPlayerElimination(loser);
                }
            }

            broadcastCombat(city, result);

            return result;
        }
    }

    /**
     *
     */
    public void repair(int amount) {
        boolean didRepair = (getHitPoints() < getUnitTypeData().getMaxHitPoints());

        _hitPoints += amount;
        if (_hitPoints > getUnitTypeData().getMaxHitPoints()) _hitPoints = getUnitTypeData().getMaxHitPoints();
        if (_hitPoints == getUnitTypeData().getMaxHitPoints() && didRepair) clearOrders();
    }

    /**
     *
     */
    public void damage(int damage) {
        _hitPoints -= damage;
    }

    /**
     *
     */
    public void destroy() {
        MapSquare square = getMapSquare();

        // Have to check for null owner because this could be the combat representation of a
        // neutral city.  isCityCombatRepresentation() check covers combat representation of a player's city.
        if (isCityCombatRepresentation()) Logger.warn("\n\nNot real unit!" + toString());
        if (square.getUnit() != this) Logger.warn("\n\nSquare doesn't own this unit!!" + toString());
        if (getOwner() != null && !isCityCombatRepresentation()) {
            getOwner().removeUnit(this);
            getOwner().updateUnitsInPlay(getClass(), -1);
            getOwner().updateUnitsLost(getClass(), 1);
        }

        if (square.getUnit() == this) square.setUnit(null);

        if (isInTransport(square)) {
            Transport transport = (Transport) square.getUnit();
            transport.unloadUnit(this);
        }

        if (square.getCity() != null) square.getCity().removeUnit(this);

        setMovementPoints(0);
        getController().onUnitDestroyed(this);
        getController().modelChanged(square);
        _dead = true;
    }

    /**
     *
     */
    public boolean isDead() {
        return _dead;
    }

    /**
     *
     */
    public void addUnitActionListener(UnitActionListener listener) {
        _listeners.add(listener);
    }

    /**
     *
     */
    public void removeUnitActionListener(UnitActionListener listener) {
        _listeners.remove(listener);
    }

    /**
     *
     */
    protected void broadcastMove(Point start, Point end) {
        for (int index = 0; index < _listeners.size(); index++) {
            ((UnitActionListener) _listeners.get(index)).unitMoved(this, start, end);
        }

        // GC is always notified.
        getController().unitMoved(this, start, end);
    }

    /**
     *
     */
    protected void broadcastCombat(OwnableObject opponent, boolean won) {
        for (int index = 0; index < _listeners.size(); index++) {
            ((UnitActionListener) _listeners.get(index)).unitFought(this, opponent, won);
        }

        getController().unitFought(this, opponent, won);
    }

    /**
     *
     */
    protected GameController getController() {
        return ConquestFactory.getController();
    }

    /**
     * The combat algorithm:
     *
     * - The attacker attacks the defender
     * - Odds of a hit are the ratio of the attacker's attack factor to the sum of that factor and the defender's defense factor.
     *
     * Example: attackFactor of 10, defenseFactor of 5, odds of attacker hit are 10/15 = .67
     * - If a hit is scored, the unit loses attacker's damage value in hit points
     * - If defender is destroyed, end
     * - The defender attacks the attacker, as above
     * - If attacker is destroyed, end
     * - Loop unil one unit is destroyed
     */
    private boolean attackImpl(UnitImpl defender) {
        Logger.info(this + " attacking " + defender);
        while (true) {
            if (attack(this, defender)) return true;
            else if (attack(defender, this)) return false;
        }
    }

    private boolean attack(UnitImpl attacker, UnitImpl defender) {
        double attackFactor = attacker.getAttackFactor(defender);
        double defenseFactor = defender.getDefenseFactor(attacker);
        double oddsOfHit = attackFactor / (attackFactor + defenseFactor);
        double roll = (getRandom() % (attackFactor + defenseFactor));
        boolean hit = roll >= oddsOfHit;

        Logger.info(attacker + " firing at " + defender);
        Logger.info("attackFactor: " + attackFactor);
        Logger.info("defenseFactor: " + defenseFactor);
        Logger.info("oddsOfHit: " + oddsOfHit);
        Logger.info("roll: " + roll + "; hit " + hit + "\n");

        if (hit) {
            int damage = attacker.getUnitTypeData().getDamage();
            defender.damage(damage);
            Logger.info("Hit scored; defender loses " + damage + " hit points; " + defender.getHitPoints() + " remaining.");
            if (defender.getHitPoints() <= 0) {
                getController().unitDefeated(attacker, defender);
                return true;
            }
        }

        return false;
    }

    /**
     *
     */
    protected double getAttackFactor(Unit defender) {
        return getUnitTypeData().getBaseAttackFactor(defender.getClass());
    }

    /**
     *
     */
    protected double getDefenseFactor(Unit attacker) {
        return getUnitTypeData().getBaseDefenseFactor(attacker.getClass());
    }

    /**
     *
     */
    protected void setFacing(Point newLocation, Point oldLocation) {
        if (newLocation.getX() < oldLocation.getX()) _flags &= ~FACING_RIGHT;
        else _flags |= FACING_RIGHT;
    }

    /**
     *
     */
    protected boolean checkForLeavingTransport(Unit unit) {
        if (unit != null && unit != this && unit instanceof Transport) {
            ((Transport) unit).unloadUnit(this);
            notInTransport();
            return true;
        }

        return false;
    }

    /**
     *
     */
    protected boolean checkForEnteringTransport(Unit unit) {
        if (unit instanceof Transport) {
            Transport transport = (Transport) unit;

            if (transport.canTransport(this)) {
                transport.loadUnit(this);
                return true;
            }
        }

        return false;
    }

    /**
     * This method checks if it's legal for this unit to move to the given location.  It differs from canMoveTo() in that it is "dynamic" rather
     * than "static"- it checks for temporary move-blockers such as friendly units in the way, as well as for sufficient movement points.
     * <p/>
     * If there is a friendly unit in the way, and the unit is on a Move To command, this method determines if the unit could move safely over the
     * blocking unit in the same turn by following it's path.  If so, the move is legal.
     *
     * @return A MoveToReturn object describing the legality of the move.
     */
    protected Unit.MoveToReturn checkMove(Point location) {
        MapSquare newSquare = getController().getMap().getSquareAt(location);
        Unit unit = newSquare.getUnit();

        if (!canMoveTo(location)) return new MoveToReturnImpl(false, Unit.MoveToReturn.ILLEGAL_SQUARE);

        // Check for sufficient movement points.
        if (getMovementPoints() < getUnitTypeData().getMovementCost(newSquare.getClass()) && getUnitTypeData().getMovementCost(newSquare.getClass()) < MapSquare.ILLEGAL_MOVE) {
            setMovementPoints(0);
            return new MoveToReturnImpl(false, Unit.MoveToReturn.OUT_OF_MOVEMENT);
        }

        // Check for friendly unit in the square.  Move is legal if it's a
        // transport this unit can board or we can move over the blocking unit.
        if (unit != null) {
            if (unit.getOwner().equals(_owner)) {
                if (unit instanceof Transport && ((Transport) unit).canTransport(this)) {
                    Transport transport = (Transport) unit;
                    if (!transport.doesUnitFit(this)) return new MoveToReturnImpl(false, Unit.MoveToReturn.FAILED_FULL_TRANSPORT);
                } else {
                    // Friendly unit in the way.  See if we can move over it.
                    if (!checkOverlappingMove(newSquare)) return new MoveToReturnImpl(false, Unit.MoveToReturn.FAILED_FRIENDLY_UNIT);
                }
            } else return new MoveToReturnImpl(false, Unit.MoveToReturn.FAILED_ENEMY_UNIT);
        }

        return new MoveToReturnImpl(true, Unit.MoveToReturn.IGNORE_THIS);
    }

    /**
     *
     */
    protected boolean checkOverlappingMove(MapSquare square) {
        UnitPath pathCopy = null;
        int movementPoints = getMovementPoints();

        // Overlapping moves only allowed for pieces on MoveTo.
        if (!hasMoveTo()) return false;

        try {
            pathCopy = (UnitPath) _path.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // Fake moving the unit through its path, to see if it can get to an unoccupied square before exhausting its movement points.
        while (square != null) {
            movementPoints -= getUnitTypeData().getMovementCost(square.getClass());

            if (movementPoints < 0) return false;
            else if (square.getUnit() == null) return true; // Made it.

            square = pathCopy.getNextSquare();
        }

        return false;
    }

    /**
     *
     */
    protected double getRandom() {
        return Math.random();
    }

    /**
     *
     */
    protected boolean isCityCombatRepresentation() {
        MapSquare square = getMapSquare();
        return square.getCity() != null && !square.getCity().isUnitPresent(this);
    }

    /**
     *
     */
    protected static void createInstances() {
        for (int index = 0; index < _classes.size(); index++) {
            Class unitClass = (Class) _classes.get(index);
            _instances.put(unitClass, ConquestFactory.createUnit(unitClass.getName(), null, null));
        }
    }

    /**
     *
     */
    protected static class MoveToReturnImpl implements Unit.MoveToReturn {
        private boolean _success;
        private int _detail;

        public MoveToReturnImpl(boolean success, int detail) {
            _success = success;
            _detail = detail;
        }

        public boolean wasSuccessful() {
            return _success;
        }

        public int getDetail() {
            return _detail;
        }
    }

    /**
     *
     */
    public static abstract class UnitTypeDataImpl implements Unit.UnitTypeData {
        public int getMaxSightRange() {
            return 2;
        }

        public int getSightRange(Class unitClass) {
            return 2;
        }

        public double getBaseAttackFactor(Class unitClass) {
            return 10;
        }

        public double getBaseDefenseFactor(Class unitClass) {
            return 10;
        }
    }
}
