package com.osi.conquest.ui.event;


import com.osi.conquest.domain.MapSquare;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public interface ModelChangeListener {
    public void modelChanged(MapSquare square);

    public void modelChanged(MapSquare[] squares);

    public void updateUI(Rectangle area);
}
