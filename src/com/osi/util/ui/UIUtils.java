package com.osi.util.ui;


import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class UIUtils {
    public static Point bottomRight(Rectangle rect) {
        Point topLeft = rect.getLocation();
        return new Point(topLeft.x + (int) rect.getWidth(), topLeft.y + (int) rect.getHeight());
    }
}
