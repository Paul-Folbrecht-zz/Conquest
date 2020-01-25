package com.osi.conquest.ui.dialogs;


import com.osi.conquest.domain.City;
import com.osi.conquest.domain.Player;
import com.osi.conquest.domain.impl.unit.UnitImpl;
import com.osi.conquest.ui.renderers.UnitRenderer;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class CityStatsDialog extends JDialog {
    protected static int COLUMN_CITY_NAME = 0;
    protected static int COLUMN_CITY_LOCATION = 1;
    protected static int COLUMN_UNIT_IN_PRODUCTION = 2;
    protected static int COLUMN_TURNS_TILL_COMPLETION = 3;

    protected Player _player;

    /**
     *
     */
    public CityStatsDialog(Frame frame, Player player) {
        super(frame, "City Statistics", true);

        _player = player;
        init();
        pack();
    }

    /**
     *
     */
    protected void init() {
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        panel.add(createTable(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        getContentPane().add(panel);
    }

    /**
     *
     */
    protected JComponent createTable() {
        JTable table = new JTable(new CityStatsTableModel());
        JScrollPane scroller = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        ((CityStatsTableModel) table.getModel()).adjustColumnWidths(table);
        scroller.getViewport().setPreferredSize(table.getPreferredSize());
        scroller.setBorder(BorderFactory.createRaisedBevelBorder());

        return scroller;
    }

    /**
     *
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton button;

        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        button = new JButton("Ok");
        getRootPane().setDefaultButton(button);
        button.addActionListener(new OkHandler());
        panel.add(button);

        return panel;
    }

    /**
     *
     */
    protected class OkHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            setVisible(false);
        }
    }

    /**
     *
     */
    protected class CityStatsTableModel extends AbstractTableModel {
        protected List _unitClasses = UnitImpl.getClasses();
        protected List _cities = _player.getCities();
        protected HashMap _icons = new HashMap();
        protected String[] _columnNames;

        public CityStatsTableModel() {
            // Init images.
            for (int index = 0; index < _unitClasses.size(); index++) {
                _icons.put(_unitClasses.get(index), new ImageIcon(UnitRenderer.getImage(_player.getId(), (Class) _unitClasses.get(index))));
            }

            // Init column names.
            _columnNames = new String[4];
            _columnNames[0] = "City";
            _columnNames[1] = "Location";
            _columnNames[2] = "Unit Producing";
            _columnNames[3] = "Turns Till Completion";
        }

        public void adjustColumnWidths(JTable table) {
            TableColumn column = table.getColumn(_columnNames[3]);
            JLabel label = new JLabel(_columnNames[3]);
            label.revalidate();
            int width = (int) label.getPreferredSize().getWidth();

            for (int index = 0; index < _columnNames.length; index++) {
                column = table.getColumn(_columnNames[index]);
                column.setMinWidth(width);
                column.setMaxWidth(width);
            }
            table.sizeColumnsToFit(0);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }

        public int getRowCount() {
            return _cities.size();
        }

        public int getColumnCount() {
            return _columnNames.length;
        }

        public String getColumnName(int index) {
            return _columnNames[index];
        }

        public Class getColumnClass(int index) {
            return getValueAt(0, index).getClass();
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public Object getValueAt(int row, int column) {
            City city = (City) _cities.get(row);

            if (column == COLUMN_CITY_NAME) {
                return city.getName();
            } else if (column == COLUMN_CITY_LOCATION) {
                return "" + city.getLocation().x + ", " + city.getLocation().y;
            } else if (column == COLUMN_UNIT_IN_PRODUCTION) {
                return _icons.get(city.getUnitTypeInProduction());
            } else if (column == COLUMN_TURNS_TILL_COMPLETION) {
                return "" + city.getTurnsTillCompletion();
            }

            return null;
        }
    }
}
