package com.osi.conquest.ui.panels;


import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.Unit;
import com.osi.util.ui.dialogs.Validatable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class UnitsPanel extends JPanel implements Validatable {
    protected List _units;
    protected JList _listBox;
    protected JButton _okButton;
    protected boolean _readOnly;

    /**
     *
     */
    public UnitsPanel(List units, boolean createButtons) {
        this(units, createButtons, false);
    }

    /**
     *
     */
    public UnitsPanel(List units, boolean createButtons, boolean readOnly) {
        _readOnly = readOnly;
        _units = units;
        init(createButtons);
    }

    /**
     *
     */
    public void setUnits(List units) {
        setVisible(units != null);
        _units = units;
        populate();
    }

    /**
     *
     */
    public void onAdded() {
        JDialog parent = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, this);
        parent.getRootPane().setDefaultButton(_okButton);
    }

    /**
     *
     */
    public boolean validateData() {
        return true;
    }

    /**
     *
     */
    public void onOk() {
    }

    /**
     *
     */
    public void onCancel() {
    }

    /**
     *
     */
    protected void populate() {
        if (_units != null) {
            _listBox.setListData(_units.toArray());
        } else {
            _listBox.setListData(new Object[0]);
        }
    }

    /**
     *
     */
    protected void init(boolean createButtons) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        add(new JLabel("Contents:"), BorderLayout.NORTH);
        add(createListPanel(), BorderLayout.CENTER);
        if (createButtons) {
            add(createButtonPanel(), BorderLayout.SOUTH);
        }
    }

    /**
     *
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton button;

        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());

        _okButton = new JButton("Ok");
        _okButton.addActionListener(new OkHandler());
        panel.add(_okButton);

        if (!_readOnly) {
            button = new JButton("Clear Orders");
            button.addActionListener(new ClearOrdersHandler());
            panel.add(button);

            button = new JButton("Move Up");
            button.addActionListener(new MoveUpHandler());
            panel.add(button);
        }

        return panel;
    }

    /**
     *
     */
    protected JScrollPane createListPanel() {
        JPanel panel = new JPanel();
        JScrollPane scroller = new JScrollPane();

        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _listBox = new JList();
        _listBox.addMouseListener(new MouseHandler());
        populate();
        scroller.getViewport().setView(_listBox);

        return scroller;
    }

    /**
     *
     */
    protected class MoveUpHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            int[] indeces = _listBox.getSelectedIndices();

            for (int index = 0; index < indeces.length; index++) {
                Object unit = _units.remove(indeces[index]);
                int newIndex = indeces[index] - 1;

                if (newIndex < 0) {
                    newIndex = 0;
                }

                _units.add(newIndex, unit);
            }

            populate();
        }
    }

    /**
     *
     */
    protected class OkHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            setVisible(false);
            int[] indeces = _listBox.getSelectedIndices();
            JDialog parent = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, UnitsPanel.this);

            if (indeces != null && indeces.length > 0 && !_readOnly) {
                ConquestFactory.getController().setSelectedUnit((Unit) _units.get(indeces[0]));
            }

            if (parent != null) {
                parent.setVisible(false);
            }
        }
    }

    /**
     *
     */
    protected class ClearOrdersHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            int[] indeces = _listBox.getSelectedIndices();
            JDialog parent = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, UnitsPanel.this);

            if (parent != null) {
                parent.setVisible(false);
            }

            for (int index = 0; index < indeces.length; index++) {
                Unit unit = (Unit) _units.get(indeces[index]);
                unit.clearOrders();
            }
        }
    }

    /**
     *
     */
    protected class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent event) {
            if (!_readOnly) {
                if (event.getClickCount() == 2) {
                    int index = _listBox.locationToIndex(event.getPoint());
                    if (index != -1) {
                        JDialog parent = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, UnitsPanel.this);

                        if (parent != null) {
                            parent.setVisible(false);
                        }
                        ConquestFactory.getController().setSelectedUnit((Unit) _listBox.getSelectedValue());
                    }
                }
            }
        }
    }
}
