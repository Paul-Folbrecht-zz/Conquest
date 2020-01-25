package com.osi.conquest.ui.dialogs.newgame;


import com.osi.conquest.ConquestException;
import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.ConquestMap;
import com.osi.conquest.domain.MapParameters;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;


/**
 * @author Paul Folbrecht
 */
public class MapPanel extends JPanel {
    protected JCheckBox _useExistingMap = new JCheckBox("Use Existing Map: ");
    protected JComboBox _maps;
    protected JCheckBox _terrainHidden = new JCheckBox("Terrain Hidden");
    protected JCheckBox _equalizeCities = new JCheckBox("Equilize Cities");
    protected JCheckBox _randomStart = new JCheckBox("Random Start Locations");
    protected JSlider _size = createDefaultSlider();
    protected JSlider _continentPositionVariance = createDefaultSlider();
    protected JSlider _continentSizeVariance = createDefaultSlider();
    protected JSlider _amountSwamp = createDefaultSlider();
    protected JSlider _amountForest = createDefaultSlider();
    protected JSlider _amountMountains = createDefaultSlider();
    protected ConquestMap _map;

    /**
     *
     */
    public MapPanel() {
        init();
        loadPrefs();
    }

    /**
     *
     */
    public ConquestMap getMap() {
        if (_map == null) {
            if (_useExistingMap.isSelected()) {
                try {
                    _map = ConquestFactory.loadMap((String) _maps.getSelectedItem());
                } catch (ConquestException e) {
                    // This will be handled by the method returning null.
                }
            } else {
                MapParameters params = new MapParameters();

                params.terrainHidden = _terrainHidden.isSelected();
                params.equalizeCities = _equalizeCities.isSelected();
                params.randomStart = _randomStart.isSelected();
                params.size = new Dimension(_size.getValue(), _size.getValue());
                params.continentPositionVariance = _continentPositionVariance.getValue();
                params.continentSizeVariance = _continentSizeVariance.getValue();
                params.amountSwamp = _amountSwamp.getValue();
                params.amountForest = _amountForest.getValue();
                params.amountMountains = _amountMountains.getValue();
                _map = ConquestFactory.createMap(params);
            }
        }

        return _map;
    }

    /**
     *
     */
    public boolean validateData() {
        if (getMap() == null) {
            JOptionPane.showMessageDialog(this, "Cannot load map!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     *
     */
    public void onOk() {
        savePrefs();
    }

    /**
     *
     */
    protected void init() {
        setLayout(new GridLayout(10, 1));
        setBorders();

        _equalizeCities.setSelected(true);
        _randomStart.setSelected(true);

        _size.setMinimum(25);
        _size.setMaximum(175);
        _size.setValue(100);
        _size.setMinorTickSpacing(25);
        _size.setMajorTickSpacing(50);

        add(createUseMapPanel());
        add(createBooleanOptionsPanel());
        add(_size);
        add(_continentPositionVariance);
        add(_continentSizeVariance);
        add(_amountSwamp);
        add(_amountForest);
        add(_amountMountains);
    }

    /**
     *
     */
    protected JComponent createUseMapPanel() {
        JPanel panel = new JPanel();
        String[] mapFilenames = new String[0];
        File mapDirectory = new File(PropertyManager.getMapPath());
        File[] mapFiles = mapDirectory.listFiles();

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        panel.add(_useExistingMap);
        _useExistingMap.addChangeListener(new UseMapChangeHandler());

        if (mapFiles != null && mapFiles.length > 0) {
            mapFilenames = new String[mapFiles.length];
            for (int index = 0; index < mapFiles.length; index++) {
                if (mapFiles[index].getName().endsWith(".cmp")) {
                    mapFilenames[index] = mapFiles[index].getName();
                }
            }
        }

        _maps = new JComboBox(mapFilenames);
        panel.add(_maps);

        return panel;
    }

    /**
     *
     */
    protected JComponent createBooleanOptionsPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        panel.add(_terrainHidden);
        panel.add(_equalizeCities);
        panel.add(_randomStart);

        return panel;
    }

    /**
     *
     */
    protected void setBorders() {
        setBorder(BorderFactory.createRaisedBevelBorder());
        _size.setBorder(BorderFactory.createTitledBorder("Map Width/Height"));
        _continentPositionVariance.setBorder(BorderFactory.createTitledBorder("Continent Position Variance"));
        _continentSizeVariance.setBorder(BorderFactory.createTitledBorder("Continent Size Variance"));
        _amountSwamp.setBorder(BorderFactory.createTitledBorder("Relative Amount of Swamp"));
        _amountForest.setBorder(BorderFactory.createTitledBorder("Relative Amount of Forest"));
        _amountMountains.setBorder(BorderFactory.createTitledBorder("Relative Amount of Mountains"));
    }

    /**
     *
     */
    protected JSlider createDefaultSlider() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);

        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);

        return slider;
    }

    /**
     *
     */
    protected void loadPrefs() {
        try {
            _useExistingMap.setSelected(PropertyManager.getPref("mapPanel.useExistingMap").
                    equalsIgnoreCase("true"));
            _terrainHidden.setSelected(PropertyManager.getPref("mapPanel.terrainHidden").
                    equalsIgnoreCase("true"));
            _equalizeCities.setSelected(PropertyManager.getPref("mapPanel.equalizeCities").
                    equalsIgnoreCase("true"));
            _randomStart.setSelected(PropertyManager.getPref("mapPanel.randomStart").
                    equalsIgnoreCase("true"));
            _size.setValue(PropertyManager.getIntPref("mapPanel.size"));
            _continentPositionVariance.setValue(PropertyManager.getIntPref("mapPanel.continentPositionVariance"));
            _continentSizeVariance.setValue(PropertyManager.getIntPref("mapPanel.continentSizeVariance"));
            _amountSwamp.setValue(PropertyManager.getIntPref("mapPanel.amountSwamp"));
            _amountForest.setValue(PropertyManager.getIntPref("mapPanel.amountForest"));
            _amountMountains.setValue(PropertyManager.getIntPref("mapPanel.amountMountains"));
        } catch (ConquestRuntimeException e) {
            // Value not found- not a problem.
        }
    }

    /**
     *
     */
    protected void savePrefs() {
        PropertyManager.setPref("mapPanel.useExistingMap", "" + _useExistingMap.isSelected());
        PropertyManager.setPref("mapPanel.terrainHidden", "" + _terrainHidden.isSelected());
        PropertyManager.setPref("mapPanel.equalizeCities", "" + _equalizeCities.isSelected());
        PropertyManager.setPref("mapPanel.randomStart", "" + _randomStart.isSelected());
        PropertyManager.setPref("mapPanel.size", "" + _size.getValue());
        PropertyManager.setPref("mapPanel.continentPositionVariance", "" +
                _continentPositionVariance.getValue());
        PropertyManager.setPref("mapPanel.continentSizeVariance", "" +
                _continentSizeVariance.getValue());
        PropertyManager.setPref("mapPanel.amountSwamp", "" + _amountSwamp.getValue());
        PropertyManager.setPref("mapPanel.amountForest", "" + _amountForest.getValue());
        PropertyManager.setPref("mapPanel.amountMountains", "" + _amountMountains.getValue());
        PropertyManager.savePrefs();
    }

    /**
     *
     */
    protected void setEnablement(boolean useExistingMap) {
        _maps.setEnabled(useExistingMap);
        _terrainHidden.setEnabled(!useExistingMap);
        _equalizeCities.setEnabled(!useExistingMap);
        _randomStart.setEnabled(!useExistingMap);
        _size.setEnabled(!useExistingMap);
        _continentPositionVariance.setEnabled(!useExistingMap);
        _continentSizeVariance.setEnabled(!useExistingMap);
        _amountSwamp.setEnabled(!useExistingMap);
        _amountForest.setEnabled(!useExistingMap);
        _amountMountains.setEnabled(!useExistingMap);
    }

    /**
     *
     */
    protected class UseMapChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            setEnablement(_useExistingMap.isSelected());
        }
    }
}
