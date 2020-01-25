package com.osi.conquest.domain.impl.mapsquare;


import com.osi.conquest.domain.MapSquare;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class Forest extends MapSquareImpl {
    protected static SquareTypeDataImpl _typeData = new SquareTypeDataImpl();
    protected static Color _worldMapColor = new Color(0, 128, 0);

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
            return "Forest";
        }

        public Color getWorldMapColor() {
            return _worldMapColor;
        }
    }
}
