package com.osi.conquest.domain.impl;


import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.*;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class ConquestMapImpl implements ConquestMap {
    private static final long serialVersionUID = 1L;
    protected List _cities = new ArrayList();
    protected List _cityNames = new ArrayList();
    protected int _cityNameIndex = 0;
    protected MapParameters _params;
    protected MapSquare[][] _mapSquares;

    /**
     *
     */
    public ConquestMapImpl(MapParameters params) {
        _params = params;
    }

    /**
     *
     */
    public MapParameters getParameters() {
        return _params;
    }

    /**
     *
     */
    public Thread init() {
        return new Thread(new InitWorker());
    }

    /**
     *
     */
    public void paint(Graphics2D graphics, boolean paintUnits) {
        Rectangle clip = null;

        if (graphics.getClip() != null) {
            clip = graphics.getClip().getBounds();
        }

        for (int x = 0; x < _params.size.getWidth(); x++) {
            for (int y = 0; y < _params.size.getHeight(); y++) {
                if (clip != null) {
                    if (((x + 1) * MapSquare.WIDTH >= clip.x) &&
                            ((x - 1) * MapSquare.WIDTH <= clip.x + clip.getWidth()) &&
                            ((y + 1) * MapSquare.HEIGHT >= clip.y) &&
                            ((y - 1) * MapSquare.HEIGHT <= clip.y + clip.getHeight())) {
                        _mapSquares[x][y].getRenderer().render(graphics, paintUnits);
                    }
                } else {
                    _mapSquares[x][y].getRenderer().render(graphics, paintUnits);
                }
            }
        }
    }

    /**
     *
     */
    public void uncoverTerrain(Point center, int range, Player player) {
        int x = (int) center.getX();
        int y = (int) center.getY();

        for (int row = x - range; row < x + range; row++) {
            for (int col = y - range; col < y + range; col++) {
                MapSquare square = getSquareAt(new Point(row, col));
                if (square != null) square.unHide(player);
            }
        }
    }

    /**
     *
     */
    public int getRange(Point start, Point end) {
        return (int) Math.max(Math.abs(start.getX() - end.getX()), Math.abs(start.getY() - end.getY()));
    }

    /**
     *
     */
    public Dimension getSize() {
        if (_mapSquares == null) return null;
        return _params.size;
    }

    /**
     *
     */
    public Dimension getSizeInPixels() {
        return new Dimension((int) _params.size.getWidth() * MapSquare.WIDTH, (int) _params.size.getHeight() * MapSquare.HEIGHT);
    }

    /**
     *
     */
    public MapSquare getSquareAt(Point location) {
        if (location != null && location.x >= 0 && location.x < _params.size.getWidth() && location.y >= 0 && location.y < _params.size.getHeight()) {
            return _mapSquares[location.x][location.y];
        }

        return null;
    }

    /**
     *
     */
    public MapSquare fastGetSquareAt(int x, int y) {
        return _mapSquares[x][y];
    }

    /**
     *
     */
    public MapSquare getSquareAtPixel(Point location) {
        Point squareLocation = new Point(location.x / MapSquare.WIDTH, location.y / MapSquare.HEIGHT);
        return getSquareAt(squareLocation);
    }

    /**
     *
     */
    public City getRandomCoastalCity() {
        int count = 0;

        while (count++ < _cities.size()) {
            int index = getRandom() % _cities.size();
            City city = (City) _cities.get(index);
            if (city.getOwner() == null && getSquareAt(city.getLocation()).isCoastal()) return city;
        }

        return null;
    }

    /**
     *
     */
    public City getStartCity(Player player) {
        City city;

        if (_params.randomStart) city = getRandomCoastalCity();
        else {
            // Get a city nearest to one of the corners of the map, based on the player.
            Point point = new Point(0, 0);

            if (player.getId() == 1) point = new Point((int) _params.size.getWidth() - 1, (int) _params.size.getHeight() - 1);
            else if (player.getId() == 2) point = new Point(0, (int) _params.size.getHeight() - 1);
            else if (player.getId() == 3) point = new Point((int) _params.size.getWidth() - 1, 0);

            city = findClosestNeutralCity(point);
        }

        // The only way this could happen is if there are fewer cities than players.
        if (city == null) city = createCityAtFirstAvailableLocation();

        return city;
    }

    /**
     *
     */
    public City findClosestFriendlyCity(Unit unit) {
        Iterator it = unit.getOwner().getCities().iterator();
        int range = 10000;
        City closestCity = null;

        while (it.hasNext()) {
            City city = (City) it.next();
            int thisRange = getRange(unit.getLocation(), city.getLocation());

            if (thisRange < range) {
                range = thisRange;
                closestCity = city;
            }
        }

        return closestCity;
    }

    /**
     *
     */
    public MapSquare findUnoccupiedSquareWithinRadius(Unit unit, int radius) {
        for (int distanceFromOrigin = 1; distanceFromOrigin <= radius; distanceFromOrigin++) {
            List squares = getSurroundingSquares(unit.getLocation(), radius);

            for (int index = 0; index < squares.size(); index++) {
                MapSquare square = getSquareAt((Point) squares.get(index));
                if (square.getUnit() == null || square.getUnit().getOwner() != unit.getOwner()) return square;
            }
        }

        return null;
    }

    /**
     *
     */
    public void resetCities() {
        Iterator it = _cities.iterator();
        List cities = new ArrayList(_cities.size());

        while (it.hasNext()) {
            City city = (City) it.next();
            City newCity = new CityImpl(city.getLocation(), city.getName());

            getSquareAt(newCity.getLocation()).setCity(newCity);
            cities.add(newCity);
        }
        _cities = cities;
    }

    /**
     *
     */
    public List getOwnableObjectsWithinRadius(Point point, int radius) {
        List objects = new ArrayList();

        for (int distance = 1; distance <= radius; distance++) {
            List squares = getSurroundingSquares(point, distance);
            for (int index = 0; index < squares.size(); index++) {
                MapSquare square = getSquareAt((Point) squares.get(index));
                if (square.getObject() != null) objects.add(square.getObject());
            }
        }

        return objects;
    }

    /**
     *
     */
    public void setCitiesAndUnits(Player[] players) {
        for (int x = 0; x < _params.size.getWidth(); x++) {
            for (int y = 0; y < _params.size.getHeight(); y++) _mapSquares[x][y].setUnit(null);
        }

        for (int index = 0; index < players.length; index++) {
            Iterator it;

            it = players[index].getCities().iterator();
            while (it.hasNext()) {
                City city = (City) it.next();
                getSquareAt(city.getLocation()).setCity(city);
                removeCity(city.getName());
                _cities.add(city);
            }

            it = players[index].getUnits().iterator();
            while (it.hasNext()) {
                Unit unit = (Unit) it.next();
                MapSquare square = getSquareAt(unit.getLocation());
                if (!unit.isInTransport(square) && square.getCity() == null) square.setUnit(unit);
            }
        }
    }

    /**
     *
     */
    protected City findClosestNeutralCity(Point point) {
        Iterator it = _cities.iterator();
        int range = 10000;
        City closestCity = null;

        while (it.hasNext()) {
            City city = (City) it.next();
            int thisRange = getRange(point, city.getLocation());

            if (thisRange < range) {
                range = thisRange;
                closestCity = city;
            }
        }

        return closestCity;
    }

    /**
     *
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        UnitPathImpl.createGraphData(this);
    }

    /**
     *
     */
    protected void init(MapParameters params) {
        _params = params;
        initCityNames();
        initMemory(params.terrainHidden);
        buildMap(params);
        UnitPathImpl.createGraphData(this);
    }

    /**
     *
     */
    protected void initCityNames() {
        while (true) {
            String name = null;

            try {
                name = PropertyManager.getProperty("city.name." + _cityNameIndex++);
            } catch (ConquestRuntimeException e) {
            }

            if (name == null) {
                if (_cityNames.size() == 0) Logger.warn("No city names found!");
                return;
            }
            _cityNames.add(name);
        }
    }

    /**
     *
     */
    protected void buildMap(MapParameters params) {
        int continentsPerRowOrCol = (int) _params.size.getWidth() / 20;
        int width = (int) _params.size.getWidth();
        int height = (int) _params.size.getHeight();
        int numCitiesPerContinent = (int) _params.size.getWidth() / 50;

        if (params.continentPositionVariance == 0) params.continentPositionVariance++;
        if (params.continentSizeVariance == 0) params.continentSizeVariance++;

        // Make each continent.  Break up the map into "zones".  There will be numXZones * numYZones zones, each with exactly one continent.
        // The continents may be large enough, however, to overflow their zones.
        for (int xZone = (width / continentsPerRowOrCol) / 2; xZone < width; xZone += width / continentsPerRowOrCol) {
            for (int yZone = (height / continentsPerRowOrCol) / 2; yZone < height; yZone += height / continentsPerRowOrCol) {
                ArrayList squares = new ArrayList();
                int x;
                int y;

                if (getRandom() % 2 == 0) x = xZone - (getRandom() % params.continentPositionVariance);
                else x = xZone + (getRandom() % params.continentPositionVariance);

                if (getRandom() % 2 == 0) y = yZone - (getRandom() % params.continentPositionVariance);
                else y = yZone + (getRandom() % params.continentPositionVariance);

                x = adjustX(x);
                y = adjustY(y);

                _mapSquares[x][y] = ConquestFactory.createMapSquare("Plain", new Point(x, y), params.terrainHidden);
                // "Grow" the continent, starting at the center.
                int size = ((width + height) / 2) + (getRandom() % params.continentSizeVariance) + 25;
                Point point = new Point(x, y);
                for (int index = 0; index < size; index++) {
                    point = adjustCoordsRandomly(point);
                    if (_mapSquares[point.x][point.y] == null) {
                        MapSquare square = ConquestFactory.createMapSquare("Plain", point, params.terrainHidden);
                        _mapSquares[point.x][point.y] = square;
                        squares.add(square);
                        index++;
                    }
                }

                if (squares.size() > 0) {
                    addTerrain("Swamp", squares, params, params.amountSwamp);
                    addTerrain("Forest", squares, params, params.amountForest);
                    addTerrain("Mountain", squares, params, params.amountMountains);
                    addCities(squares, numCitiesPerContinent);
                }
            }
        }

        fillInWater(params.terrainHidden);
    }

    /**
     *
     */
    protected void addTerrain(String type, ArrayList continentSquares,
                              MapParameters params, int amount) {
        if (getRandom() % 100 <= amount) {
            int numBlocks = getRandom() % 3 + 1;
            for (int index = 0; index < numBlocks; index++) {
                // Pick the starting square randomly from the squares in this continent.
                int squareIndex = getRandom() % continentSquares.size();
                for (int index2 = 0; index2 < continentSquares.size() % 10; index2++) {
                    MapSquare square = (MapSquare) continentSquares.get(squareIndex);
                    Point location = square.getLocation();

                    _mapSquares[location.x][location.y] = ConquestFactory.createMapSquare(type, square.getLocation(), params.terrainHidden);
                    continentSquares.set(squareIndex, _mapSquares[location.x][location.y]);
                    squareIndex++;
                    if (squareIndex >= continentSquares.size()) squareIndex = 0;
                }
            }
        }
    }

    /**
     *
     */
    protected void addCities(ArrayList continentSquares, int numCities) {
        int index = 0;

        while (index < numCities) {
            MapSquare square = (MapSquare) continentSquares.get(getRandom() % continentSquares.size());

            if (square.getCity() == null) {
                // Want all cities to be on a Plain square.
                square = ConquestFactory.createMapSquare("Plain", square.getLocation(), _params.terrainHidden);
                _mapSquares[square.getLocation().x][square.getLocation().y] = square;
                square.setCity(new CityImpl(square.getLocation(), getNextCityName()));
                _cities.add(square.getCity());
                index++;
            } else Logger.info("City already present at this location.");
        }
    }

    /**
     *
     */
    protected String getNextCityName() {
        _cityNameIndex++;

        if (_cityNameIndex >= _cityNames.size()) return "City " + _cityNameIndex;
        else return (String) _cityNames.get(_cityNameIndex);
    }

    /**
     *
     */
    protected void initMemory(boolean isHidden) {
        _mapSquares = new MapSquare[(int) _params.size.getWidth()][(int) _params.size.getHeight()];
    }

    /**
     *
     */
    protected void fillInWater(boolean isHidden) {
        for (int x = 0; x < _params.size.getWidth(); x++) {
            for (int y = 0; y < _params.size.getHeight(); y++) {
                if (_mapSquares[x][y] == null) {
                    _mapSquares[x][y] = ConquestFactory.createMapSquare("Water", new Point(x, y), isHidden);
                }
            }
        }
    }

    /**
     *
     */
    protected Point adjustCoordsRandomly(Point point) {
        int random = getRandom() % 8;
        int x = point.x;
        int y = point.y;

        if (random == 0) x--;
        else if (random == 1) x++;
        else if (random == 2) y--;
        else if (random == 3) y++;
        else if (random == 4) {
            x--;
            y--;
        } else if (random == 5) {
            x--;
            y++;
        } else if (random == 6) {
            x++;
            y--;
        } else if (random == 7) {
            x++;
            y++;
        }

        return new Point(adjustX(x), adjustY(y));
    }

    /**
     *
     */
    protected int adjustX(int x) {
        if (x < 0) return 0;
        else if (x >= _params.size.getWidth()) return (int) _params.size.getWidth() - 1;
        else return x;
    }

    /**
     *
     */
    protected int adjustY(int y) {
        if (y < 0) return 0;
        else if (y >= _params.size.getHeight()) return (int) _params.size.getHeight() - 1;
        else return y;
    }

    /**
     *
     */
    protected int getRandom() {
        return (int) (Math.random() * 1000);
    }

    /**
     *
     */
    protected void removeCity(String name) {
        for (int index = 0; index < _cities.size(); index++) {
            City city = (City) _cities.get(index);
            if (city.getName().equals(name)) {
                _cities.remove(index);
                return;
            }
        }
    }

    /**
     *
     */
    protected void dump() {
        System.out.println("\n\nDumping Map:\n");

        for (int y = 0; y < _params.size.getHeight(); y++) {
            StringBuffer buffer = new StringBuffer((int) _params.size.getWidth());

            for (int x = 0; x < _params.size.getWidth(); x++) {
                MapSquare square = _mapSquares[x][y];

                if (square == null) buffer.append("!");
                else if (square.isWater()) buffer.append("W");
                else buffer.append("L");
            }

            System.out.println(buffer.toString());
        }
    }

    /**
     *
     */
    protected City createCityAtFirstAvailableLocation() {
        for (int x = 0; x < _params.size.getWidth(); x++) {
            for (int y = 0; y < _params.size.getHeight(); y++) {
                if (_mapSquares[x][y].getCity() == null) {
                    City city = new CityImpl(_mapSquares[x][y].getLocation(), getNextCityName());
                    _mapSquares[x][y].setCity(city);
                    _cities.add(city);
                    return city;
                }
            }
        }

        return null;
    }

    /**
     *
     */
    protected List getSurroundingSquares(Point origin, int distance) {
        List list = new ArrayList();
        Point point = new Point();

        // Horizontal lines.
        for (int innerDistance = 0; innerDistance <= distance; innerDistance++) {
            point.setLocation(origin.x - innerDistance, origin.y - distance);
            addPointIfValid(point, list);
            if (innerDistance != 0) {
                point.setLocation(origin.x + innerDistance, origin.y - distance);
                addPointIfValid(point, list);
            }

            point.setLocation(origin.x - innerDistance, origin.y + distance);
            addPointIfValid(point, list);
            if (innerDistance != 0) {
                point.setLocation(origin.x + innerDistance, origin.y + distance);
                addPointIfValid(point, list);
            }
        }

        // Vertical lines- skip corner cause we got them above.
        for (int innerDistance = 0; innerDistance < distance; innerDistance++) {
            point.setLocation(origin.x - distance, origin.y - innerDistance);
            addPointIfValid(point, list);
            if (innerDistance != 0) {
                point.setLocation(origin.x - distance, origin.y + innerDistance);
                addPointIfValid(point, list);
            }

            point.setLocation(origin.x + distance, origin.y - innerDistance);
            addPointIfValid(point, list);
            if (innerDistance != 0) {
                point.setLocation(origin.x + distance, origin.y + innerDistance);
                addPointIfValid(point, list);
            }
        }

        return list;
    }

    /**
     *
     */
    private void addPointIfValid(Point location, List list) {
        if (getSquareAt(location) != null) {
            list.add(new Point(location));
        }
    }

    /**
     *
     */
    protected class InitWorker implements Runnable {
        public void run() {
            init(_params);
        }
    }
}
