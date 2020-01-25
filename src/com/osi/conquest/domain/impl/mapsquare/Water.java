package com.osi.conquest.domain.impl.mapsquare;


import com.osi.conquest.domain.MapSquare;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class Water extends MapSquareImpl {
    protected static SquareTypeDataImpl _typeData = new SquareTypeDataImpl();

    /**
     *
     */
    public boolean isWater() {
        return true;
    }

    /**
     *
     */
    public MapSquare.SquareTypeData getSquareTypeData() {
        return _typeData;
    }

    /**
     *
     */
    public static class SquareTypeDataImpl implements MapSquare.SquareTypeData {
        public String getName() {
            return "Water";
        }

        public Color getWorldMapColor() {
            return Color.blue;
        }
    }
}
