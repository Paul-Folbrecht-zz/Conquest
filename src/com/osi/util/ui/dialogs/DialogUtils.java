package com.osi.util.ui.dialogs;


import javax.swing.*;
import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class DialogUtils {
    public static void showCentered(JFrame frame, JDialog dlg) {
        Dimension frameSize = frame.getSize();
        Dimension dlgSize = dlg.getSize();
        Point location;

        location = new Point((int) ((frameSize.getWidth() / 2) - (dlgSize.getWidth() / 2)),
                (int) ((frameSize.getHeight() / 2) - (dlgSize.getHeight() / 2)));
        location = new Point(location.x + frame.getLocation().x, location.y + frame.getLocation().y);

        if (location.x < 0) {
            location.x = 0;
        }
        if (location.y < 0) {
            location.y = 0;
        }

        dlg.setLocation(location);
        dlg.setVisible(true);
    }
}
