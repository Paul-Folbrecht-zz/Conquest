package com.osi.conquest.ui.panels;


import com.osi.conquest.domain.City;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class CityStatsPanel extends JPanel {
    protected City _city;
    protected JTable _table;

    /**
     *
     */
    public CityStatsPanel() {
        init();
        validate();
    }

    /**
     *
     */
    public void setCity(City city) {
        _city = city;
        populate();

        setVisible(city != null);
    }

    /**
     *
     */
    protected void populate() {
        _table.tableChanged(new TableModelEvent(_table.getModel()));
        _table.sizeColumnsToFit(0);
    }

    /**
     *
     */
    protected void init() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        add(new JLabel("City Info:"), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
    }

    /**
     *
     */
    protected JComponent createTable() {
        _table = new JTable(new CityStatsTableModel());
        JScrollPane scroller = new JScrollPane(_table, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        _table.setTableHeader(null);
        scroller.getViewport().setPreferredSize(_table.getPreferredSize());

        return scroller;
    }

    /**
     *
     */
    protected class CityStatsTableModel extends AbstractTableModel {
        private static final int COLS = 2;
        private static final int ROWS = 5;
        private static final int NAME_COLUMN = 0;
        private static final int DATA_COLUMN = 1;
        private static final int NAME_ROW = 0;
        private static final int LOCATION_ROW = 1;
        private static final int PRODUCTION_ROW = 2;
        private static final int TIME_ROW = 3;
        private static final int HAS_AUTOFORWARD_ROW = 4;
        private String[] _names =
                {"Name:", "Location:", "Producing:", "Time To Completion:", "Has AutoForward:"};

        public CityStatsTableModel() {
        }

        public int getRowCount() {
            return ROWS;
        }

        public int getColumnCount() {
            return COLS;
        }

        public String getColumnName(int index) {
            return "";
        }

        public Class getColumnClass(int index) {
            return getValueAt(0, index).getClass();
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public Object getValueAt(int row, int column) {
            if (column == NAME_COLUMN) {
                return _names[row];
            } else if (column == DATA_COLUMN) {
                return getData(row);
            }

            return null;
        }

        protected String getData(int row) {
            if (_city == null) {
                return "";
            }

            if (row == NAME_ROW) {
                return _city.getName();
            } else if (row == LOCATION_ROW) {
                Point loc = _city.getLocation();
                return "" + loc.x + ", " + loc.y;
            } else if (row == PRODUCTION_ROW) {
                return _city.getProductionRepresentation().getUnitTypeData().getName();
            } else if (row == TIME_ROW) {
                return "" + _city.getTurnsTillCompletion();
            } else if (row == HAS_AUTOFORWARD_ROW) {
                if (_city.getAutoForwardPath() != null) {
                    return "Yes";
                } else {
                    return "No";
                }
            }

            return null;
        }
    }
}
