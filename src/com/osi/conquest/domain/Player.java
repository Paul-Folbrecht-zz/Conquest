package com.osi.conquest.domain;


import java.awt.*;
import java.io.Serializable;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public interface Player extends Serializable, Cloneable {
    public static final int MAX_PLAYERS = 6;

    void init();

    void dump();

    public String getName();

    public Color getColor();

    public int getId();

    public List getUnits();

    public List getCities();

    public int getCurrentTurn();

    public int getUnitsInProduction(Class unitClass);

    public int getUnitsInPlay(Class unitClass);

    public int getUnitsKilled(Class unitClass);

    public int getUnitsLost(Class unitClass);

    public int getTimeTillNextDone(Class unitClass);

    public Unit getFirstUnitWithMovement();

    public boolean isLocalToHost();

    public boolean isConnected();

    public void connected();

    public void addMessage(String from, String msg);

    public void updateLastSightedObject(Point location, OwnableObject object);

    public void updateCurrentSightedObject(Point location, OwnableObject object);

    public OwnableObject getCurrentSightedObject(Point location);

    public OwnableObject getLastSightedObject(Point location);

    public boolean isObjectSighted(OwnableObject object, Point location);

    public void updateUnitsInProduction(Class unitClass, int delta);

    public void updateUnitsInPlay(Class unitClass, int delta);

    public void updateUnitsKilled(Class unitClass, int delta);

    public void updateUnitsLost(Class unitClass, int delta);

    public void addCity(City city);

    public void removeCity(City city);

    public void addUnit(Unit unit);

    public void removeUnit(Unit unit);

    public void doProduction();

    public void doProduction(City city);

    public void preActivation();

    public void onActivate();

    public void onDeactivate();

    public void onStartTurn();

    public void onEndTurn();

    public void paintProductionModeSymbols(Graphics2D graphics);

    public void addMovementReport(MovementReport report);

    public boolean hasMovementReports();

    public void playMovementReports();
}
