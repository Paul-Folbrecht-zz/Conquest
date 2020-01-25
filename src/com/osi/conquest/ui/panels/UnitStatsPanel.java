package com.osi.conquest.ui.panels;


import com.osi.conquest.domain.Unit;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class UnitStatsPanel extends JPanel {
    protected Unit _unit;
    protected JTable _table;
    protected UnitStatsTableModel _model;

    /**
     *
     */
    public UnitStatsPanel() {
        init();
        validate();
    }

    /**
     *
     */
    public void setUnit(Unit unit) {
        setVisible(unit != null);
        _unit = unit;
        _model.refreshUnitData();
        populate();
    }

    /**
     *
     */
    public void populate() {
        _table.tableChanged(new TableModelEvent(_table.getModel()));
        _table.sizeColumnsToFit(0);
    }

    /**
     *
     */
    protected void init() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        add(new JLabel("Unit Info:"), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
    }

    /**
     *
     */
    protected JComponent createTable() {
        _model = new UnitStatsTableModel();
        _table = new JTable(_model);
        JScrollPane scroller = new JScrollPane(_table, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        _table.setTableHeader(null);
        scroller.getViewport().setPreferredSize(_table.getPreferredSize());

        return scroller;
    }

    /**
     *
     */
    protected class UnitStatsTableModel extends AbstractTableModel {
        private static final int COLS = 2;
        private static final int NAME_COLUMN = 0;
        private static final int DATA_COLUMN = 1;
        private String[][] _unitData = null;

        public void refreshUnitData() {
            if (_unit != null) {
                _unitData = _unit.getDisplayableInfo();
            } else {
                _unitData = null;
            }
        }

        public int getRowCount() {
            if (_unit == null) {
                return 6;
            }
            return _unit.getDisplayableInfo().length;
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
            if (_unitData == null) {
                return "";
            }

            if (row < _unitData.length) {
                return _unitData[row][column];
            } else {
                return "";
            }
        }
    }
}
