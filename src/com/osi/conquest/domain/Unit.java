package com.osi.conquest.domain;


import com.osi.conquest.domain.event.UnitActionListener;

import java.awt.*;
import java.io.Serializable;


/**
 * @author Paul Folbrecht
 */

public interface Unit extends OwnableObject {
    public static final int MAX_UNIT_RANGE = 6;
    public static final int NUM_UNIT_TYPES = 11;

    public static interface MoveToReturn extends Serializable {
        public static final int IGNORE_THIS = 0;
        public static final int ERROR_OUTOFGAS = 1;
        public static final int STACK_UNSTACKED = 2;
        public static final int OUT_OF_MOVEMENT = 3;
        public static final int ILLEGAL_SQUARE = 4;
        public static final int FOUND_ENEMY = 5;
        public static final int FAILED_FRIENDLY_UNIT = 6;
        public static final int FAILED_ENEMY_UNIT = 7;
        public static final int FAILED_ENEMY_CITY = 8;
        public static final int FAILED_FULL_TRANSPORT = 9;

        public boolean wasSuccessful();

        public int getDetail();
    }

    public static interface UnitTypeData extends Serializable {
        public String getName();

        public int getMaxMovementPoints();

        public int getMaxHitPoints();

        public int getMaxRange();

        public int getMaxSightRange();

        public int getSightRange(Class unitClass);

        public int getTimeToProduce();

        public int getDamage();

        public int getMovementCost(Class mapSquareType);

        public boolean canAttackCity();

        public double getBaseAttackFactor(Class unitClass);

        public double getBaseDefenseFactor(Class unitClass);

        public int getTransportWeight();
    }

    public static interface Renderer {
        public void render(Graphics2D graphics);

        public void renderMirage(Graphics2D graphics);
    }

    // General Accessors.
    public Renderer getRenderer();

    public int getId();

    public String[][] getDisplayableInfo();

    public Player getOwner();

    public Point getLocation();

    public boolean isOnSentry();

    public String getOrders();

    public Rectangle getArea();

    public Rectangle getSightArea();

    public UnitTypeData getUnitTypeData();

    public String getStatusText();

    public int getUnitCount();

    public int getHitPoints();

    public int getMovementPoints();

    public int getRangeTo(Point location);

    public boolean isAdjacentTo(Point location);

    public boolean canMoveTo(Point location);

    public boolean canAttack(Point location);

    public boolean canIntentionallyAttack(Point location);

    public boolean hasMoveTo();

    public UnitPath getPath();

    public boolean isOnPatrol();

    public boolean isInTransport(MapSquare square);

    public boolean isDead();

    public boolean isFacingRight();

    // General mutators.
    public void init(Player owner, Point location);

    public void resetMovement();

    public void resetDamage();

    public void setMovementPoints(int points);

    public void decrementMovementPoints(int points);

    public void damage(int damage);

    public void setLocation(Point location);

    public void setMovePath(UnitPath path);

    public void clearOrders();

    public void clearMoveTo();

    public void onPatrol();

    public void offPatrol();

    public void inTransport();

    public void notInTransport();

    public boolean updateSightedStatus(boolean updateUI);

    public MoveToReturn moveOneSquare(Point location);

    public boolean executeMoveTo();

    public boolean select();

    public boolean deselect();

    public void sentry();

    public void unSentry();

    public boolean attack(Unit unit);

    public boolean attack(City city);

    public void repair(int amount);

    public void destroy();

    public void addUnitActionListener(UnitActionListener listener);

    public void removeUnitActionListener(UnitActionListener listener);
}
