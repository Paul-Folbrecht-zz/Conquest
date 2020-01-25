package com.osi.conquest.domain.impl;


import com.osi.conquest.ConquestException;
import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.Logger;
import com.osi.conquest.domain.*;
import com.osi.conquest.ui.MainWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public abstract class GameControllerImpl implements GameController {
    private static final long serialVersionUID = 1L;
    protected Player[] _players;
    protected HashMap _movementReportsInProgress = new HashMap();
    protected Unit _selectedUnit;
    protected int _currentPlayerIndex = -1;
    protected boolean _isDirty = true;
    protected boolean _gameStarted = false;
    protected String _mapFile = "";
    protected transient ConquestMap _map;
    protected transient MainWindow _mainWindow = MainWindow.getInstance();

    /**
     *
     */
    public GameControllerImpl() {
    }

    /**
     *
     */
    public void scrollTo(Point point) {
        MainWindow.getInstance().scrollTo(point);
    }

    /**
     *
     */
    public void modelChanged(MapSquare square) {
        MainWindow.getInstance().modelChanged(square);
    }

    /**
     *
     */
    public void modelChanged(MapSquare[] squares) {
        MainWindow.getInstance().modelChanged(squares);
    }

    /**
     *
     */
    public void updateUI(Rectangle area) {
        MainWindow.getInstance().updateUI(area);
    }

    /**
     *
     */
    public void waitCursorOn() {
        MainWindow.getInstance().waitCursorOn();
    }

    /**
     *
     */
    public void waitCursorOff() {
        MainWindow.getInstance().waitCursorOff();
    }

    public void displayPlayerMessage(String from, String msg) {
        MainWindow.getInstance().displayPlayerMessage(from, msg);
    }

    /**
     *
     */
    public ConquestMap getMap() {
        return _map;
    }

    /**
     *
     */
    public void loadMap() throws ConquestException {
        _map = ConquestFactory.loadMap(_mapFile);
        if (_map == null) throw new ConquestException("Cannot find map: " + _mapFile);
        _map.setCitiesAndUnits(getPlayers());
    }

    /**
     *
     */
    public String getMapFilename() {
        return _mapFile;
    }

    /**
     *
     */
    public void setMapFilename(String name) {
        _mapFile = name;
    }

    /**
     *
     */
    public Player[] getPlayers() {
        return _players;
    }

    /**
     *
     */
    public Unit getSelectedUnit() {
        return _selectedUnit;
    }

    /**
     *
     */
    public void setSelectedUnit(Unit unit) {
        if (_selectedUnit != null) deselectUnit();

        _selectedUnit = unit;
        if (unit != null) unit.select();
    }

    /**
     *
     */
    public void deselectUnit() {
        if (getSelectedUnit() != null) {
            _selectedUnit.deselect();
            updateUI(_selectedUnit.getArea());
        }
        _selectedUnit = null;
    }

    /**
     * Delegates to MainWindow.
     */
    public void showError(String msg, Exception e) {
        MainWindow.getInstance().showError(msg, e);
    }

    /**
     *
     */
    public void showCityDialog(Player player, City city, boolean readOnly) {
        scrollTo(city.getLocation());
        MainWindow.getInstance().showCityDialog(player, city, readOnly);
    }

    /**
     *
     */
    public void onUnitOutOfGas(Unit unit) {
        MainWindow.getInstance().onUnitOutOfGas(unit);
        checkForPlayerElimination(unit.getOwner());
    }

    /**
     *
     */
    public void onUnitDestroyed(Unit unit) {
        MainWindow.getInstance().onUnitDestroyed(unit);

        // Must eradicate all references to this unit by any Player.
        for (int index = 0; index < _players.length; index++) {
            // Note that we're assuming that if player X's unit dies during player Y's turn, player Y
            // killed it (this is a safe assumption).  Otherwise, it would not be appropriate to
            // always remove the last sighting.  Since player Y killed the unit, he must know its actual location.
            if (unit.equals(_players[index].getCurrentSightedObject(unit.getLocation()))) {
                _players[index].updateCurrentSightedObject(unit.getLocation(), null);
            }
        }
        checkForPlayerElimination(unit.getOwner());
    }

    /**
     *
     */
    public void onCityDefeated(City city) {
        if (city.getOwner() != null) checkForPlayerElimination(city.getOwner());
    }

    /**
     *
     */
    public void setGameDirty() {
        _isDirty = true;
    }

    /**
     *
     */
    public void init(ConquestMap map, Player[] players) {
        _map = map;
        _players = players;
        _currentPlayerIndex = 0;
        _gameStarted = true;
    }

    /**
     * Handles the starting of a new game- meaning this machine is the host.
     */
    public void newGame() {
        assignStartCities();
        initPlayers();
    }

    private void initPlayers() {
        for (int index = 0; index < _players.length; index++) {
            _players[index].init();
        }
    }

    /**
     *
     */
    public void gameRestored() {
    }

    /**
     * @param players Passed here because the controller might not have been initialized yet.
     */
    public boolean anyRemotePlayers(Player[] players) {
        for (int index = 0; index < players.length; index++) {
            if (players[index] == null || !players[index].isLocalToHost()) return true;
        }

        return false;
    }

    /**
     *
     */
    public abstract void receiveGameState(int playerIndex, Player[] players);

    /**
     *
     */
    public abstract void playerEliminated(Player player);

    /**
     * Activates the current player.  The current player is changed via nextPlayer().
     */
    public void activateLocalPlayer() {
        deselectUnit();
        getCurrentPlayer().preActivation();
        MainWindow.getInstance().onPlayerActivated();
        getCurrentPlayer().onActivate();
    }

    /**
     *
     */
    public void deactivateCurrentPlayer() {
        if (getCurrentPlayer() != null) getCurrentPlayer().onDeactivate();
    }

    /**
     * Note this function is called after each game turn for each player.  A player's "turn", the amount of time spent in continuous session, may
     * encompass several game turns.  After each game turn, this function determines if it is possible to allow the current player to play another
     * turn.  This is based on the player's proximity to enemy units/cities.
     */
    public void endOfTurn() {
        boolean turnOver = false;

        deselectUnit();
        setGameDirty();

        // If this is player 1, we need to decide if he can take another turn or not. This is decided by whether or not any two players are close enough together
        // that one could reach another and still get to move another turn. The number of turns that player 1 gets is the number that everyone else will get this time
        // through.  So, the distance between all of player m's units and cities to all of player n's units and cities must be computed for all players.
        if (_currentPlayerIndex == 0) turnOver = !checkPlayerProximity();
        else {
            // Other players go until they're at same turn as player 1.
            if (getCurrentPlayer().getCurrentTurn() == _players[0].getCurrentTurn()) turnOver = true;
        }

        getCurrentPlayer().onEndTurn();
        if (turnOver) {
            deactivateCurrentPlayer();
            activateNextPlayer();
        } else {
            getCurrentPlayer().onStartTurn();
        }
    }

    /**
     *
     */
    public Player getCurrentPlayer() {
        if (_players != null && _currentPlayerIndex < _players.length) return _players[_currentPlayerIndex];
        return null;
    }

    /**
     *
     */
    public Player getPlayerWithId(int id) {
        for (int index = 0; index < _players.length; index++) {
            if (_players[index].getId() == id) return _players[index];
        }

        return null;
    }

    /**
     *
     */
    public int getCurrentTurn() {
        return getCurrentPlayer().getCurrentTurn();
    }

    /**
     * This function iterates through the array of units for the current player, starting at the unit after the currently selected unit, and attempts to
     * select another unit for movement.  It first goes through all units, attempting to move all on MoveTo commands.  If one is reached that can't
     * be moved, or completes its MoveTo, that unit is selected.  Else, the units are gone through again, and the first non-sentried, non-MoveTo unit
     * is selected.
     * <p/>
     * The function return true if there is a unit selected when it returns; false otherwise.
     */
    public boolean nextUnit(Unit unitToSkip) {
        Unit unit = null;
        Unit unitToSelect = null;
        Iterator it = getCurrentPlayer().getUnits().iterator();

        setGameDirty();
        if (unitToSkip == null) unitToSkip = getSelectedUnit();
        deselectUnit();

        // Move any units on Move To, stopping on the first one that can't be moved.
        while (it.hasNext()) {
            unit = (Unit) it.next();
            if (unit.hasMoveTo() && unit.getMovementPoints() > 0) {
                scrollTo(unit.getLocation());
                if (!unit.executeMoveTo()) {
                    // Unit was not successfully moved, sighted an enemy unit while moving, or completed its Move To with movement remaining - break the loop.
                    // (This unit will have been selected in ExtendedMoveTo().)
                    unitToSelect = unit;
                    break;
                } else if (unit.isDead()) {
                    // Must start over because the iterator is now invalid.
                    it = getCurrentPlayer().getUnits().iterator();
                }
            }
        }

        // Find the first unit with movement points left.
        if (unitToSelect == null) {
            it = getCurrentPlayer().getUnits().iterator();
            while (it.hasNext()) {
                unit = (Unit) it.next();

                if (unit.getMovementPoints() > 0 && !unit.isOnSentry() && !unit.hasMoveTo() && unit != unitToSkip) {
                    unitToSelect = unit;
                    break;
                }
            }
        }

        if (unitToSelect != null) {
            unit.setMovePath(null);
            setSelectedUnit(unit);
            scrollTo(unit.getLocation());
        }

        if (unitToSelect == null) noUnitsToMove();

        return (unitToSelect != null);
    }

    /**
     *
     */
    public void noUnitsToMove() {
        if (MainWindow.getInstance().promptEndTurn()) {
            endOfTurn();
        }
    }

    /**
     *
     */
    public void removePlayer(Player player) {
        Player[] players = new Player[_players.length - 1];
        Player currentPlayer = _players[_currentPlayerIndex];
        int newIndex = 0;

        if (players.length == 0) MainWindow.getInstance().endCurrentGame(false);

        // Destroy all the player's units and reset his cities to neutral.
        for (int index = 0; index < player.getCities().size(); index++) {
            ((City) player.getCities().get(index)).setOwner(null);
        }
        for (int index = 0; index < player.getUnits().size(); index++) {
            ((Unit) player.getUnits().get(index)).destroy();
        }

        for (int index = 0; index < _players.length; index++) {
            if (_players[index] != player) players[newIndex++] = _players[index];
        }
        _players = players;

        if (player == currentPlayer) {
            _currentPlayerIndex--;
            // Current player is now the player *before* the one just eliminated.  nextPlayer() should be called to advance to the next player.
        } else {
            _currentPlayerIndex = playerIdToArrayIndex(currentPlayer.getId());
        }
    }

    /**
     * Intended to be used only in Production Mode, this function will cause all
     * of the player's cities to paint themselves with the bitmap of the unit
     * they are currently producing on top of the city image.
     */
    public void paintProductionModeSymbols(Graphics2D graphics) {
        getCurrentPlayer().paintProductionModeSymbols(graphics);
    }

    /**
     *
     */
    public boolean combat(Graphics2D graphics, Unit attacker, Point location) {
        MapSquare square = _map.getSquareAt(location);
        boolean result = false;

        if (square.getCity() != null) result = attacker.attack(square.getCity());
        else if (square.getUnit() != null) result = attacker.attack(square.getUnit());
        else Logger.warn("combat(): nothing to attack!");

        modelChanged(getMap().getSquareAt(attacker.getLocation()));
        modelChanged(square);
        updateUI(attacker.getArea().union(square.getArea()));

        return result;
    }

    /**
     *
     */
    public void unitDefeated(Unit winner, Unit loser) {
        loser.destroy();
        if (winner.getOwner() != null) winner.getOwner().updateUnitsKilled(loser.getClass(), 1);
        if (loser.getOwner() != null) {
            MapSquare square = getMap().getSquareAt(winner.getLocation());
            if (square.getCity() == null) loser.getOwner().updateCurrentSightedObject(winner.getLocation(), winner);
        }
    }

    /**
     *
     */
    public void unitMoved(Unit unit, Point start, Point end) {
        List objects = getMap().getOwnableObjectsWithinRadius(end, 2);
        for (int index = 0; index < objects.size(); index++) {
            OwnableObject object = (OwnableObject) objects.get(index);
            if (object.getOwner() != null && object.getOwner() != unit.getOwner()) {
                if (object.isWithinSightRange(unit) && getReportInProgress(object, unit) == null) startReport(object, unit);
            }
        }
    }

    /**
     *
     */
    public void unitFought(Unit unit, OwnableObject opponent, boolean won) {
        if (opponent.getOwner() != null) {
            if (getReportInProgress(opponent, unit) == null) startReport(opponent, unit);
        }
    }

    /**
     *
     */
    public MovementReport startReport(OwnableObject watcher, Unit unit) {
        List reports = (List) _movementReportsInProgress.get(watcher);
        MovementReport report = new MovementReport(watcher, unit);

        if (reports == null) {
            reports = new ArrayList();
            _movementReportsInProgress.put(watcher, reports);
        }
        reports.add(report);

        return report;
    }

    /**
     *
     */
    public void endReport(OwnableObject watcher, MovementReport report) {
        List reports = (List) _movementReportsInProgress.get(watcher);
        reports.remove(report);
        report.end();
        watcher.getOwner().addMovementReport(report);
    }

    /**
     *
     */
    public void checkForPlayerElimination(Player player) {
        if (player != null && player.getCities().size() == 0 && player.getUnits().size() == 0) {
            playerEliminated(player);
        }
    }

    /**
     *
     */
    protected MovementReport getReportInProgress(OwnableObject watcher, Unit unit) {
        List reports = (List) _movementReportsInProgress.get(watcher);

        if (reports != null) {
            for (int index = 0; index < reports.size(); index++) {
                MovementReport report = (MovementReport) reports.get(index);
                if (report.getWatched().equals(unit)) return report;
            }
        }

        return null;
    }

    /**
     *
     */
    protected void endMovementReports() {
        Iterator keys = _movementReportsInProgress.keySet().iterator();
        while (keys.hasNext()) {
            OwnableObject object = (OwnableObject) keys.next();
            List list = (List) _movementReportsInProgress.get(object);

            for (int index = 0; index < list.size(); index++) {
                MovementReport report = (MovementReport) list.get(index);
                object.getOwner().addMovementReport(report);
                report.end();
            }
        }
        _movementReportsInProgress.clear();
    }

    /**
     * This method advances the game state to the next player.
     */
    protected void nextPlayer() {
        endMovementReports();
        _currentPlayerIndex += 1;
        if (_currentPlayerIndex >= _players.length) _currentPlayerIndex = 0;
    }

    /**
     *
     */
    protected abstract void activateNextPlayer();


    /**
     * @return true if all units & cities are far enough apart that it is safe to let player 1 take another turn.
     */
    protected boolean checkPlayerProximity() {
        for (int index = 0; index < _players.length; index++) {
            for (int index2 = 0; index2 < _players.length; index2++) {
                if (index != index2) {
                    Iterator it = _players[index].getUnits().iterator();
                    int rangeLimit = (Unit.MAX_UNIT_RANGE * 2) * (1 + Math.abs(_players[index].getCurrentTurn() - _players[index2].getCurrentTurn()));

                    while (it.hasNext()) {
                        Point unitLocation = ((Unit) it.next()).getLocation();
                        Iterator it2 = _players[index2].getUnits().iterator();

                        // Check range of this unit to all enemy units.
                        while (it2.hasNext()) {
                            Unit unit = (Unit) it2.next();
                            if (_map.getRange(unitLocation, unit.getLocation()) < rangeLimit) return false;
                        }

                        // Check range of this unit to all enemy cities.
                        it2 = _players[index2].getCities().iterator();
                        while (it2.hasNext()) {
                            City city = (City) it2.next();
                            if (_map.getRange(unitLocation, city.getLocation()) < rangeLimit) return false;
                        }
                    }

                    it = _players[index].getCities().iterator();
                    while (it.hasNext()) {
                        Point unitLocation = ((City) it.next()).getLocation();
                        Iterator it2 = _players[index2].getUnits().iterator();

                        // Check range of this city to all enemy units.
                        while (it2.hasNext()) {
                            Unit unit = (Unit) it2.next();
                            if (_map.getRange(unitLocation, unit.getLocation()) < rangeLimit) return false;
                        }

                        // Check range of this city to all enemy cities.
                        it2 = _players[index2].getCities().iterator();
                        while (it2.hasNext()) {
                            City city = (City) it2.next();
                            if (_map.getRange(unitLocation, city.getLocation()) < rangeLimit) return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     *
     */
    protected void assignStartCities() {
        for (int index = 0; index < _players.length; index++) {
            City city = _map.getStartCity(_players[index]);

            city.setOwner(_players[index]);
            _players[index].addCity(city);
            _map.uncoverTerrain(city.getLocation(), city.getSightRepresentation().getUnitTypeData().getMaxSightRange(), _players[index]);
        }
    }

    /**
     *
     */
    protected Player getPreviousPlayer() {
        int index = _currentPlayerIndex - 1;
        if (index < 0) index = _players.length - 1;
        return _players[index];
    }

    /**
     *
     */
    protected int playerIdToArrayIndex(int playerIndex) {
        for (int index = 0; index < _players.length; index++) {
            if (_players[index].getId() == playerIndex) return index;
        }

        throw new ConquestRuntimeException("Invalid playerIndex: " + playerIndex);
    }
}
