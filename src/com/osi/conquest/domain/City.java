package com.osi.conquest.domain;


import java.awt.*;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public interface City extends OwnableObject {
    public static interface Renderer {
        public void render(Graphics2D graphics);
    }

    public List getUnits();

    public Player getOwner();

    public Point getLocation();

    public Class getUnitTypeInProduction();

    public int getTurnsTillCompletion();

    public int getTurnsTillCompletion(Class unitClass);

    public Unit getNewUnit();

    public boolean isProductionContinuous();

    public UnitPath getAutoForwardPath();

    public Unit getSightRepresentation();

    public Unit getProductionRepresentation();

    public Unit getFirstDefender();

    public String getName();

    public Color getDisplayableColor();

    public boolean isUnitPresent(Unit unit);

    public Renderer getRenderer();

    public void addUnit(Unit unit);

    public void removeUnit(Unit unit);

    public void setOwner(Player owner);

    public void decrementProduction();

    public void resetProduction();

    public void setProduction(Class unitType);

    public void setContinuousProduction(boolean continuous);

    public void setAutoForwardPath(UnitPath path);

    public void repairUnits();
}
