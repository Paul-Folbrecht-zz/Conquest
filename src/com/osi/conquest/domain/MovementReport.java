package com.osi.conquest.domain;


import com.osi.conquest.domain.event.UnitActionListener;
import com.osi.conquest.domain.impl.GameControllerImpl;
import com.osi.conquest.ui.MainWindow;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class MovementReport implements UnitActionListener, Serializable {
    protected OwnableObject _watcher;
    protected Point _startLocation;
    protected Unit _watched;
    protected List _events = new ArrayList();

    /**
     *
     */
    public MovementReport(OwnableObject watcher, Unit watched) {
        _watcher = watcher;
        _watched = (Unit) watched.copy();
        _startLocation = watched.getLocation();
        watched.addUnitActionListener(this);
    }

    /**
     *
     */
    public Unit getWatched() {
        return _watched;
    }

    /**
     *
     */
    public void unitMoved(Unit unit, Point start, Point end) {
        if (_watcher.isWithinSightRange(unit)) {
            _events.add(new MoveEvent(unit, start, end));
        }

        if (unit.getMovementPoints() == 0 || !_watcher.isWithinSightRange(unit)) {
            getController().endReport(_watcher, this);
        }
    }

    /**
     *
     */
    public void unitFought(Unit unit, OwnableObject opponent, boolean watchedWon) {
        if (_watcher.isWithinSightRange(unit)) {
            _events.add(new CombatEvent(unit, opponent, watchedWon));
        }

        if (_watched.getMovementPoints() == 0) {
            getController().endReport(_watcher, this);
        }
    }

    /**
     *
     */
    public Point play() {
        Point lastPoint = _startLocation;

        MainWindow.getInstance().setState(MainWindow.STATE_PLAYING_MOVEMENT_REPORTS);
        moveUnit(_watched, _startLocation, _startLocation);
        for (int index = 0; index < _events.size(); index++) {
            lastPoint = ((UnitEvent) _events.get(index)).show();
        }
        eraseUnit(lastPoint);
        MainWindow.getInstance().setState(MainWindow.STATE_NORMAL);

        return lastPoint;
    }

    /**
     *
     */
    public boolean overlaps(MovementReport other) {
        if (!_watched.equals(other._watched)) {
            return false;
        }

        for (int index = 0; index < _events.size(); index++) {
            for (int index2 = 0; index2 < other._events.size(); index2++) {
                if (_events.get(index).equals(other._events.get(index2))) {
                    // Found an Event these two Reports have in common.  Now, to be considered overlapping,
                    // each subsequent event in each Report must be identical, until the point where one
                    // of them ends.
                    int sharedPathIndex = 0;
                    while (true) {
                        if (sharedPathIndex + index >= _events.size() ||
                                sharedPathIndex + index2 >= other._events.size()) {
                            // We went completely through one Report- we have an overlap.
                            return true;
                        } else if (_events.get(sharedPathIndex + index).equals(other._events.get(sharedPathIndex + index2)) == false) {
                            return false;
                        }

                        sharedPathIndex++;
                    }
                }
            }
        }

        return false;
    }

    /**
     *
     */
    public MovementReport merge(MovementReport other) {
        int startOverlapIndex = -1;
        int endOverlapIndex = -1;

        for (int index = 0; index < _events.size(); index++) {
            if (_events.get(index).equals(other._events.get(0))) {
                startOverlapIndex = index;
                break;
            }
        }
        for (int index = 0; index < _events.size(); index++) {
            if (_events.get(index).equals(other._events.get(other._events.size() - 1))) {
                endOverlapIndex = index;
                break;
            }
        }

        if (startOverlapIndex == -1) {
            if (other.overlaps(this)) {
                return other.merge(this);
            } else {
                return null;
            }
        } else {
            // Case 1: XXXXXXXX
            //             XXXXXXX
            if (endOverlapIndex == -1) {
                int divergeIndex = _events.size() - startOverlapIndex;

                for (int index = divergeIndex; index < other._events.size(); index++) {
                    _events.add(other._events.get(index));
                }
            }
            // Case 2: XXXXXXXX
            //           XXXX
            else {
                // Nothing to do.
            }

            return this;
        }
    }

    /**
     *
     */
    public void end() {
        _watched.removeUnitActionListener(this);
    }

    /**
     *
     */
    private void moveUnit(Unit unit, Point start, Point end) {
        MapSquare startSquare = getController().getMap().getSquareAt(start);
        MapSquare endSquare = getController().getMap().getSquareAt(end);

        if (startSquare != endSquare) startSquare.setUnit(null);
        endSquare.setHiddenUnit(endSquare.getUnit());
        endSquare.setUnit(unit);
        unit.setLocation(end);

        getController().modelChanged(startSquare);
        getController().modelChanged(endSquare);
        getController().scrollTo(end);
        getController().updateUI(startSquare.getArea().union(endSquare.getArea()));
        pause();
    }

    /**
     *
     */
    private void eraseUnit(Point location) {
        MapSquare square = getController().getMap().getSquareAt(location);

        square.setUnit(null);
        getController().modelChanged(square);
        getController().updateUI(square.getArea());
    }

    /**
     *
     */
    private void pause() {
        try {
            Thread.currentThread().sleep(500);
        } catch (InterruptedException e) {
        }
    }

    /**
     *
     */
    private GameControllerImpl getController() {
        return (GameControllerImpl) ConquestFactory.getController();
    }

    /**
     *
     */
    private abstract class UnitEvent implements Serializable {
        public abstract Point show();

        protected boolean equals(Point one, Point two) {
            return (one.x == two.x && one.y == two.y);
        }
    }

    /**
     *
     */
    private class MoveEvent extends UnitEvent {
        private Unit _unit;
        private Point _start;
        private Point _end;

        public MoveEvent(Unit unit, Point start, Point end) {
            _unit = (Unit) unit.copy();
            _start = start;
            _end = end;
        }

        public boolean equals(Object object) {
            if (!(object instanceof MoveEvent)) {
                return false;
            }
            MoveEvent other = (MoveEvent) object;
            return (equals(_start, other._start) && equals(_end, other._end));
        }

        public Point show() {
            moveUnit(_unit, _start, _end);
            return _end;
        }
    }

    /**
     *
     */
    private class CombatEvent extends UnitEvent {
        private Unit _unit;
        private OwnableObject _opponent;
        private boolean _watchedWon;

        public CombatEvent(Unit unit, OwnableObject opponent, boolean watchedWon) {
            _unit = (Unit) unit.copy();
            _opponent = (OwnableObject) opponent.copy();
            _watchedWon = watchedWon;
        }

        public boolean equals(Object object) {
            if (!(object instanceof CombatEvent)) {
                return false;
            }
            CombatEvent other = (CombatEvent) object;
            return (other._opponent.equals(_opponent) && other._watchedWon == _watchedWon);
        }

        public Point show() {
            if (_opponent instanceof Unit) {
                moveUnit((Unit) _opponent, _opponent.getLocation(), _opponent.getLocation());
            }
            pause();
            eraseUnit(_opponent.getLocation());
            if (!_watchedWon) {
                eraseUnit(_unit.getLocation());
            }

            return _opponent.getLocation();
        }
    }
}
