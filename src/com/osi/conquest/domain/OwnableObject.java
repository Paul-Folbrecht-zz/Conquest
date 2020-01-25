package com.osi.conquest.domain;


import java.awt.*;
import java.io.Serializable;


/**
 * @author Paul Folbrecht
 */
public interface OwnableObject extends Serializable, Cloneable {
    Player getOwner();

    Point getLocation();

    Object copy();

    boolean isWithinSightRange(OwnableObject object);
}
