package com.osi.conquest.domain.impl;


import com.osi.conquest.Logger;
import com.osi.conquest.domain.*;
import com.osi.conquest.domain.impl.unit.LandUnit;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class UnitPathImpl implements UnitPath {
    protected static final int UNSEEN = Short.MAX_VALUE - 2;

    protected static Point[][] _dad;
    protected static int[][] _val;
    protected static List[][] _adjacencyLists;
    protected Point _from;
    protected Point _to;
    protected Point _lastPointAdded;
    protected Unit _unit;
    protected int _pathIndex;
    protected List _path = new ArrayList();
    protected List _priorityQueue = new ArrayList();
    protected boolean _skipOccupiedSquares;

    /**
     *
     */
    public static void createGraphData(ConquestMap map) {
        Dimension size = map.getSize();

        _dad = new Point[(int) size.getWidth()][(int) size.getHeight()];
        _val = new int[(int) size.getWidth()][(int) size.getHeight()];
        _adjacencyLists = new List[(int) size.getWidth()][(int) size.getHeight()];

        for (int x = 0; x < size.getWidth(); x++) {
            for (int y = 0; y < size.getHeight(); y++) {
                _adjacencyLists[x][y] = new ArrayList();
                addSquaresToAdjacencyList(map, _adjacencyLists[x][y], x, y);
            }
        }
    }

    /**
     *
     */
    public UnitPathImpl(Point from, Point to, Unit unit) {
        _from = from;
        _to = to;
        _unit = unit;
    }

    /**
     *
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     *
     */
    public void reset() {
        _pathIndex = 0;
    }

    /**
     *
     */
    public MapSquare getNextSquare() {
        _pathIndex++;

        if (_pathIndex >= _path.size()) {
            return null;
        }

        return getMap().getSquareAt((Point) _path.get(_pathIndex));
    }

    /**
     *
     */
    public MapSquare getLastSquare() {
        if (_path.size() == 0) {
            return null;
        }

        return getMap().getSquareAt((Point) _path.get(_path.size() - 1));
    }

    /**
     *
     */
    public void moveToPreviousSquare() {
        _pathIndex--;

        if (_pathIndex < 0) {
            _pathIndex = _path.size() - 1;
        }
    }

    /**
     *
     */
    public int getLength() {
        return _path.size();
    }

    /**
     *
     */
    public Rectangle getBoundingRect() {
        Rectangle rect = new Rectangle(0, 0, 0, 0);

        for (int index = 0; index < _path.size(); index++) {
            MapSquare square = getMap().getSquareAt((Point) _path.get(index));
            rect = rect.union(toDisplay(square.getArea()));
        }

        return rect;
    }

    /**
     * @todo This doesn't belong here- UI knowledge.
     */
    public Rectangle toDisplay(Rectangle area) {
        return new Rectangle(area.x * MapSquare.WIDTH, area.y * MapSquare.HEIGHT,
                area.width * MapSquare.WIDTH, area.height * MapSquare.HEIGHT);
    }

    /**
     *
     */
    public void paint(Graphics2D graphics) {
        graphics.setPaint(Color.white);
        graphics.setStroke(new BasicStroke(1));

        for (int index = _pathIndex; index < (_path.size() - 1); index++) {
            MapSquare square = getMap().getSquareAt((Point) _path.get(index));
            MapSquare nextSquare = getMap().getSquareAt((Point) _path.get(index + 1));

            drawLine(graphics, square, nextSquare);
        }

        // Only draw the last line connecting tail to head if the path is a patrol path.
        if (_unit.isOnPatrol()) {
            MapSquare square = getMap().getSquareAt((Point) _path.get(_path.size() - 1));
            MapSquare nextSquare = getMap().getSquareAt((Point) _path.get(0));

            drawLine(graphics, square, nextSquare);
        }
    }

    /**
     *
     */
    public void erase(Graphics2D graphics) {
        for (int index = _pathIndex; index < _path.size(); index++) {
            MapSquare square = getMap().getSquareAt((Point) _path.get(index));
            square.getRenderer().render(graphics);
        }
    }

    /**
     *
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(1000);
        Iterator it = _path.iterator();

        buffer.append("Path:\n");
        while (it.hasNext()) {
            Point point = (Point) it.next();
            buffer.append(point.toString() + "\n");
        }

        return buffer.toString();
    }

    /**
     *
     */
    public boolean calculatePath(boolean skipOccupiedSquares) {
        try {
            ConquestFactory.getController().waitCursorOn();

            _path.clear();
            _skipOccupiedSquares = skipOccupiedSquares;

            // First, see if a simple path will work- but not for land units. (A simple (direct) path
            // will almost never work for land units.
            if (!(_unit instanceof LandUnit)) {
                if (!skipOccupiedSquares && calculateSimplePath()) {
                    return true;
                }
            }

            return calculateDijkstraPath();
        } finally {
            ConquestFactory.getController().waitCursorOff();
        }
    }

    /**
     *
     */
    public void add(UnitPath other) {
        // Tack other's path onto the end of this one, by copying each square.
        for (int index = 1; index < other.getPath().size(); index++) {
            // Ignore the first square in the other path- it should be the last square
            // in this, and we don't want it there twice.
            _path.add(other.getPath().get(index));
        }
    }

    /**
     *
     */
    public List getPath() {
        return _path;
    }

    /**
     *
     */
    protected void drawLine(Graphics2D graphics, MapSquare from, MapSquare to) {
        Point squareCenter = from.getCenterPixelPoint();
        Point nextSquareCenter = to.getCenterPixelPoint();
        Line2D line = new Line2D.Float(new Point2D.Float(squareCenter.x, squareCenter.y),
                new Point2D.Float(nextSquareCenter.x, nextSquareCenter.y));

        graphics.draw(line);
    }

    /**
     *
     */
    protected boolean calculateSimplePath() {
        boolean done = false;
        Point endPoint = _to;
        Point nextPoint = _from;

        // This algorithm attempts to find a direct path, and fails if this is not possible.
        _path.add(_from);
        while (!done) {
            MapSquare nextSquare;
            boolean squareOk = false;

            if (nextPoint.x > endPoint.x) {
                nextPoint = new Point(nextPoint.x - 1, nextPoint.y);
            } else if (nextPoint.x < endPoint.x) {
                nextPoint = new Point(nextPoint.x + 1, nextPoint.y);
            }

            if (nextPoint.y > endPoint.y) {
                nextPoint = new Point(nextPoint.x, nextPoint.y - 1);
            } else if (nextPoint.y < endPoint.y) {
                nextPoint = new Point(nextPoint.x, nextPoint.y + 1);
            }

            nextSquare = getMap().getSquareAt(nextPoint);
            if (_unit.getUnitTypeData().getMovementCost(nextSquare.getClass()) != MapSquare.ILLEGAL_MOVE) {
                if (nextSquare.getCity() == null || nextSquare.getCity().getOwner() == _unit.getOwner()) {
                    _path.add(nextSquare.getLocation());
                    squareOk = true;
                }
            }

            if (!squareOk) {
                _path.clear();
                Logger.info("Simple path failed for unit " + _unit.getId() + " on square " + nextSquare);
                return false;
            }

            // Did we just complete the path?
            if (checkEquals(nextSquare.getLocation(), _to)) {
                Logger.info("Successfully calculated simple path for unit " + _unit.getId());
                done = true;
            }
        }

        return true;
    }

    /**
     * This is the priority-queue breadth-first-search shortest path algorithm.
     */
    protected boolean calculateDijkstraPath() {
        Point point = _from;

        initGraphData();

        if (priorityQueueUpdate(point, -UNSEEN)) {
            _dad[point.x][point.y] = null;
        }

        while (_priorityQueue.size() > 0) {
            Point point2 = priorityQueueRemove();
            List adjacencyList;

            if (point2 == null) {
                buildPath();
                Logger.info("Dijkstra path failed for unit " + _unit.getId() +
                        "\nPartial path: " + toString());
                return false;
            } else if (checkEquals(point2, _to)) {
                buildPath();
                Logger.info("Successfully calculated Dijkstra path for unit " + _unit.getId());
                return true;
            }

            point = point2;
            _val[point.x][point.y] = -_val[point.x][point.y];
            if (_val[point.x][point.y] == UNSEEN) {
                _val[point.x][point.y] = 0;
            }

            adjacencyList = _adjacencyLists[point.x][point.y];
            for (int index = 0; index < adjacencyList.size(); index++) {
                Point adjacentPoint = (Point) adjacencyList.get(index);
                MapSquare adjacentSquare = getMap().getSquareAt(adjacentPoint);

                if (_val[adjacentPoint.x][adjacentPoint.y] < 0) {
                    int priority = _val[point.x][point.y] + _unit.getUnitTypeData().
                            getMovementCost(adjacentSquare.getClass()) + 1 +
                            getPriorityAdjustment(adjacentPoint);

                    if (priorityQueueUpdate(adjacentPoint, priority)) {
                        _val[adjacentPoint.x][adjacentPoint.y] = -priority;
                        _dad[adjacentPoint.x][adjacentPoint.y] = point2;
                        _lastPointAdded = adjacentPoint;
                    }
                }
            }
        }

        buildPath();
        Logger.info("Dijkstra path failed for unit " + _unit.getId() +
                "\nPartial path: " + toString());
        return false;
    }

    /**
     *
     */
    protected Point priorityQueueRemove() {
        // Remove the square with the lowest priority.
        int lowPriority = Short.MAX_VALUE;
        Point returnPoint = null;

        if (_priorityQueue.size() > 0) {
            returnPoint = (Point) _priorityQueue.get(0);
        }

        // The start square is a special case.
        if (checkEquals(returnPoint, _from)) {
            return (Point) _priorityQueue.remove(0);
        }

        for (int index = 0; index < _priorityQueue.size(); index++) {
            Point point = (Point) _priorityQueue.get(index);
            MapSquare square = getMap().getSquareAt(point);
            int movementCost = _unit.getUnitTypeData().getMovementCost(square.getClass());
            int checkValue = -(_val[point.x][point.y]) + movementCost + 1 +
                    getPriorityAdjustment(point);

            if (checkValue < lowPriority) {
                lowPriority = checkValue;
                returnPoint = point;
            }
        }

        if (lowPriority >= MapSquare.ILLEGAL_MOVE) {
            return null;
        } else {
            _priorityQueue.remove(returnPoint);
            return returnPoint;
        }
    }

    /**
     *
     */
    protected boolean priorityQueueUpdate(Point checkPoint, int priority) {
        // Search for the square at point.  If it's in the list, but has
        // priority larger than priority, it's priority is changed to priority.
        // If it's not in the list, it's inserted.
        for (int index = 0; index < _priorityQueue.size(); index++) {
            Point point = (Point) _priorityQueue.get(index);

            if (checkEquals(point, checkPoint)) {
                if (_val[checkPoint.x][checkPoint.y] > priority) {
                    _val[checkPoint.x][checkPoint.y] = priority;
                    return true;
                } else {
                    return false;
                }
            }
        }

        // Point wasn't found; insert it.
        MapSquare square = getMap().getSquareAt(checkPoint);
        if (_unit.getUnitTypeData().getMovementCost(square.getClass()) !=
                MapSquare.ILLEGAL_MOVE || checkEquals(square.getLocation(), _from)) {
            if (!_skipOccupiedSquares || square.getUnit() == null) {
                _priorityQueue.add(checkPoint);
                return true;
            }
        }

        return false;
    }

    /**
     *
     */
    protected void buildPath() {
        Point point = _to;

        // Build the list of MapSquares from the "dad" array, working from the end.
        _path.add(_to);
        while (_dad[point.x][point.y] != null) {
            _path.add(0, _dad[point.x][point.y].getLocation());
            point = _dad[point.x][point.y].getLocation();
        }
    }

    /**
     *
     */
    protected int getPriorityAdjustment(Point point) {
        int delta = 0;
        MapSquare square = getMap().getSquareAt(point);

        if (_lastPointAdded == null) {
            // We're on the first square being added to the path- the one previous to this square is the start square.
            _lastPointAdded = _from;
        }

        if (point.x != _lastPointAdded.x && point.y != _lastPointAdded.y) {
            // This is a diagonal edge.
            delta++;
        }

        if (square.getUnit() != null && _skipOccupiedSquares) {
            delta += MapSquare.ILLEGAL_MOVE;
        } else if (square.getCity() != null && square.getCity().getOwner() != _unit.getOwner() && !checkEquals(square.getLocation(), _from)) {
            // Can't move over non-friendly cities.
            delta += MapSquare.ILLEGAL_MOVE;
        } else if (!square.isVisibleToPlayer(_unit.getOwner())) {
            delta += MapSquare.ILLEGAL_MOVE;
        }

        return delta;
    }

    /**
     *
     */
    protected boolean checkEquals(Point one, Point two) {
        return (one.x == two.x && one.y == two.y);
    }

    /**
     *
     */
    protected void initGraphData() {
        for (int x = 0; x < getMap().getSize().getWidth(); x++) {
            for (int y = 0; y < getMap().getSize().getHeight(); y++) {
                _dad[x][y] = null;
                _val[x][y] = -UNSEEN;
            }
        }
    }

    /**
     *
     */
    protected static void addSquaresToAdjacencyList(ConquestMap map, List list, int x, int y) {
        Point point = new Point();

        point.setLocation(x - 1, y);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
        point.setLocation(x - 1, y - 1);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
        point.setLocation(x, y - 1);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
        point.setLocation(x + 1, y - 1);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
        point.setLocation(x + 1, y);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
        point.setLocation(x + 1, y + 1);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
        point.setLocation(x, y + 1);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
        point.setLocation(x - 1, y + 1);
        if (map.getSquareAt(point) != null) {
            list.add(new Point(point));
        }
    }

    /**
     *
     */
    protected ConquestMap getMap() {
        return ConquestFactory.getController().getMap();
    }
}
