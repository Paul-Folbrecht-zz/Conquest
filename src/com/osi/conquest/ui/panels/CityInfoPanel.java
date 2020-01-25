package com.osi.conquest.ui.panels;


import com.osi.conquest.domain.City;

import javax.swing.*;
import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class CityInfoPanel extends JPanel {
    protected City _city = null;
    protected CityStatsPanel _cityPanel = new CityStatsPanel();
    protected UnitsPanel _unitsPanel = new UnitsPanel(null, false);

    /**
     *
     */
    public CityInfoPanel() {
        init();
        validate();
        setVisible(false);
    }

    /**
     *
     */
    public void setCity(City city) {
        _city = city;
        populate();
    }

    /**
     *
     */
    protected void init() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        add(_cityPanel, BorderLayout.CENTER);
        add(_unitsPanel, BorderLayout.SOUTH);
    }

    /**
     *
     */
    protected void populate() {
        _cityPanel.setCity(_city);
        if (_city != null) {
            _unitsPanel.setUnits(_city.getUnits());
        } else {
            _unitsPanel.setUnits(null);
        }
    }
}
