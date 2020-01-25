package com.osi.conquest.domain;


import java.awt.*;
import java.io.Serializable;


/**
 * @author Paul Folbrecht
 */
public class MapParameters implements Serializable {
    public Dimension size;
    public boolean terrainHidden;
    public boolean equalizeCities;
    public boolean randomStart;
    public int continentPositionVariance;
    public int continentSizeVariance;
    public int amountSwamp;
    public int amountForest;
    public int amountMountains;
}
