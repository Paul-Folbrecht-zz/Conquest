package com.osi.conquest.domain.impl.mapsquare;


import com.osi.conquest.domain.MapSquare;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class Plain extends MapSquareImpl {
    protected static SquareTypeDataImpl _typeData = new SquareTypeDataImpl();

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
            return "Plain";
        }

        public Color getWorldMapColor() {
            return Color.green;
        }
    }
}
