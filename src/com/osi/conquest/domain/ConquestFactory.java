package com.osi.conquest.domain;


import com.osi.conquest.ConquestException;
import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.impl.*;
import com.osi.conquest.ui.MainWindow;
import com.osi.conquest.ui.renderers.CityRenderer;
import com.osi.conquest.ui.renderers.MapSquareRenderer;
import com.osi.conquest.ui.renderers.UnitRenderer;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;


/**
 * @author Paul Folbrecht
 */
public class ConquestFactory {
    protected static GameController _controller;

    /**
     *
     */
    public static void setController(GameController controller) {
        _controller = controller;
    }

    /**
     *
     */
    public static GameController createController(boolean isHost) {
        if (isHost) {
            _controller = new HostGameController();
        } else {
            _controller = new ClientGameController();
        }

        return _controller;
    }

    /**
     *
     */
    public static GameController getController() {
        return _controller;
    }

    /**
     *
     */
    public static ConquestMap createMap(MapParameters params) {
        return new ConquestMapImpl(params);
    }

    /**
     *
     */
    public static ConquestMap loadMap(String filename) throws ConquestException {
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(PropertyManager.getMapPath() + filename));
            ConquestMapImpl map;

            MainWindow.getInstance().waitCursorOn();
            map = (ConquestMapImpl) stream.readObject();
            // The map may have been saved in a state with cities owned by players.  I don't correct
            // that at save time because then the map would have to be copied (a game is in progress).
            // It's corrected here.
            map.resetCities();

            return map;
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new ConquestException("Invalid .cmp file format.");
        } finally {
            MainWindow.getInstance().waitCursorOff();
        }
    }

    /**
     *
     */
    public static Player createPlayer(int id, String name, boolean isLocalToHost) {
        return new PlayerImpl(id, name, isLocalToHost);
    }

    /**
     *
     */
    public static Unit createUnit(String className, Player player, Point location)
            throws ConquestRuntimeException {
        try {
            Object object = Class.forName(className).newInstance();

            if (object instanceof Unit) {
                ((Unit) object).init(player, location);

                return (Unit) object;
            } else {
                throw new ConquestRuntimeException("Created a " + className + " but it's not a Unit!");
            }
        } catch (ClassNotFoundException e) {
            throw new ConquestRuntimeException("Could not find class " + className);
        } catch (Exception e) {
            throw new ConquestRuntimeException("Could not instantiate class " + className + ": " + e);
        }
    }

    /**
     *
     */
    public static MapSquare createMapSquare(String type, Point location,
                                            boolean isHidden) throws ConquestRuntimeException {
        String className = "com.osi.conquest.domain.impl.mapsquare." + type;

        try {
            Object object = Class.forName(className).newInstance();

            if (object instanceof MapSquare) {
                ((MapSquare) object).setLocation(location);
                ((MapSquare) object).setHidden(isHidden);
                return (MapSquare) object;
            } else {
                throw new ConquestRuntimeException("Created a " + className + " but it's not a MapSquare!");
            }
        } catch (ClassNotFoundException e) {
            throw new ConquestRuntimeException("Could not find class " + className);
        } catch (Exception e) {
            throw new ConquestRuntimeException("Could not instantiate class " + className);
        }
    }

    /**
     *
     */
    public static MapSquare.Renderer createMapSquareRenderer(MapSquare square) {
        return new MapSquareRenderer(square);
    }

    /**
     *
     */
    public static City.Renderer createCityRenderer(City city) {
        return new CityRenderer(city);
    }

    /**
     *
     */
    public static Unit.Renderer createUnitRenderer(Unit unit) {
        return new UnitRenderer(unit);
    }

    /**
     *
     */
    public static UnitPath createUnitPath(Point from, Point to, Unit unit) {
        return new UnitPathImpl(from, to, unit);
    }
}
