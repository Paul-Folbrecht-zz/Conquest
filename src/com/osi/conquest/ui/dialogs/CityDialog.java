package com.osi.conquest.ui.dialogs;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.Player;
import com.osi.conquest.domain.impl.unit.UnitImpl;
import com.osi.conquest.ui.MainWindow;
import com.osi.conquest.ui.panels.UnitsPanel;
import com.osi.conquest.ui.renderers.UnitRenderer;
import com.osi.util.StringUtils;
import com.osi.util.ui.dialogs.DialogUtils;
import com.osi.util.ui.dialogs.PanelWrapperDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class CityDialog extends JDialog {
    protected Player _player;
    protected City _city;
    protected Class _typeToProduce;
    protected boolean _readOnly;

    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel _buttonPanel = new JPanel();
    JButton _ok = new JButton();
    JButton _cancel = new JButton();
    JPanel _topPanel = new JPanel();
    JLabel _name = new JLabel();
    JPanel _mainPanel = new JPanel();
    JButton _units = new JButton();
    BorderLayout borderLayout2 = new BorderLayout();

    public CityDialog(Frame frame, Player player, City city) {
        this(frame, player, city, false);
    }

    public CityDialog(Frame frame, Player player, City city, boolean readOnly) {
        super(frame, "City Production", true);

        _player = player;
        _city = city;
        _readOnly = readOnly;

        try {
            jbInit();
            init();
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void init() {
        List classes = UnitImpl.getClasses();
        ButtonGroup group = new ButtonGroup();

        _name.setText(_city.getName() + " at " + _city.getLocation().x + ", " + _city.getLocation().y);
        _units.setEnabled(_city.getUnits().size() > 0);
        _typeToProduce = _city.getUnitTypeInProduction();

        _mainPanel.setLayout(new GridLayout(classes.size(), 1));
        for (int index = 0; index < classes.size(); index++) {
            Class unitClass = (Class) classes.get(index);
            String buttonText = StringUtils.stripClassName(unitClass) + " - " +
                    _city.getTurnsTillCompletion(unitClass) + " turns";
            JRadioButton button = new JRadioButton(buttonText);
            JPanel rowPanel = new JPanel();

            group.add(button);
            button.addActionListener(new UnitButtonHandler(unitClass));
            button.setSelected(_city.getUnitTypeInProduction().equals(unitClass));
            _mainPanel.add(button);
            _mainPanel.add(new JLabel(new ImageIcon(UnitRenderer.getImage(_player.getId(), unitClass))));
            if (_readOnly) {
                button.setEnabled(false);
            }
        }

        if (_readOnly) {
            _cancel.setEnabled(false);
        }
    }

    void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        _ok.setText("Ok");
        getRootPane().setDefaultButton(_ok);
        _ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _ok_actionPerformed(e);
            }
        });
        _cancel.setText("Cancel");
        _cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _cancel_actionPerformed(e);
            }
        });
        _name.setText("Name");
        panel1.setBorder(BorderFactory.createRaisedBevelBorder());
        _mainPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        _mainPanel.setLayout(borderLayout2);
        _units.setText("Units");
        _units.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _units_actionPerformed(e);
            }
        });
        this.getContentPane().add(panel1, BorderLayout.CENTER);
        panel1.add(_buttonPanel, BorderLayout.SOUTH);
        _buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        _buttonPanel.add(_ok, null);
        _buttonPanel.add(_cancel, null);
        _buttonPanel.add(_units, null);
        panel1.add(_topPanel, BorderLayout.NORTH);
        _topPanel.add(_name, null);
        panel1.add(_mainPanel, BorderLayout.CENTER);
    }

    void _units_actionPerformed(ActionEvent event) {
        UnitsPanel panel = new UnitsPanel(_city.getUnits(), true, _readOnly);
        PanelWrapperDialog dialog =
                new PanelWrapperDialog(MainWindow.getInstance(), "Conquest", true, panel, new String[0]);

        DialogUtils.showCentered(MainWindow.getInstance(), dialog);
    }

    protected class UnitButtonHandler implements ActionListener {
        protected Class _unitClass;

        public UnitButtonHandler(Class unitClass) {
            _unitClass = unitClass;
        }

        public void actionPerformed(ActionEvent event) {
            _typeToProduce = _unitClass;
        }
    }

    void _ok_actionPerformed(ActionEvent e) {
        if (_city.getUnitTypeInProduction().equals(_typeToProduce) == false) {
            _city.setProduction(_typeToProduce);
        }

        setVisible(false);
    }

    void _cancel_actionPerformed(ActionEvent e) {
        setVisible(false);
    }
}
