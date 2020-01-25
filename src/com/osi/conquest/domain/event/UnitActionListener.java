package com.osi.conquest.domain.event;


import com.osi.conquest.domain.OwnableObject;
import com.osi.conquest.domain.Unit;

import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public interface UnitActionListener {
    public void unitMoved(Unit unit, Point start, Point end);

    public void unitFought(Unit unit, OwnableObject opponent, boolean won);
}
