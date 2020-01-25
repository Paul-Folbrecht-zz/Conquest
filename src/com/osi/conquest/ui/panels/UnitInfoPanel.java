package com.osi.conquest.ui.panels;


import com.osi.conquest.domain.Transport;
import com.osi.conquest.domain.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Paul Folbrecht
 */
public class UnitInfoPanel extends JPanel {
    protected Unit _unit = null;
    protected UnitStatsPanel _unitPanel = new UnitStatsPanel();
    protected UnitsPanel _unitsPanel = new UnitsPanel(null, false);
    protected Timer _timer;

    /**
     *
     */
    public UnitInfoPanel() {
        init();
        validate();
        setVisible(false);
        _timer = new Timer(500, new RepopulateAction());
        _timer.start();
    }

    /**
     *
     */
    public void setUnit(Unit unit) {
        _unit = unit;
        populate();
    }

    /**
     *
     */
    protected void init() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        add(_unitPanel, BorderLayout.NORTH);
        add(_unitsPanel, BorderLayout.CENTER);
    }

    /**
     *
     */
    protected void populate() {
        _unitPanel.setUnit(_unit);

        if (_unit instanceof Transport) {
            _unitsPanel.setUnits(((Transport) _unit).getUnits());
        } else {
            _unitsPanel.setUnits(null);
        }
    }

    /**
     *
     */
    protected class RepopulateAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            populate();
        }
    }
}
