package com.osi.conquest.ui.dialogs;


import com.osi.conquest.domain.Player;
import com.osi.conquest.domain.impl.unit.UnitImpl;
import com.osi.conquest.ui.renderers.UnitRenderer;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class UnitStatsDialog extends JDialog {
    protected static int COLUMN_UNIT_TYPE = 0;
    protected static int COLUMN_IN_PRODUCTION = 1;
    protected static int COLUMN_IN_PLAY = 2;
    protected static int COLUMN_KILLED = 3;
    protected static int COLUMN_LOST = 4;
    protected static int COLUMN_TIME_TILL_NEXT_DONE = 5;
    protected Player _player;

    /**
     *
     */
    public UnitStatsDialog(Frame frame, Player player) {
        super(frame, "Unit Statistics", true);

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
        panel.add(createTable(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        getContentPane().add(panel);
    }

    /**
     *
     */
    protected JComponent createTable() {
        JTable table = new JTable(new UnitStatsTableModel());
        JScrollPane scroller = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        ((UnitStatsTableModel) table.getModel()).adjustColumnWidths(table);
        scroller.getViewport().setPreferredSize(table.getPreferredSize());

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
    protected class UnitStatsTableModel extends AbstractTableModel {
        protected List _unitClasses = UnitImpl.getClasses();
        protected ImageIcon[] _icons;
        protected String[] _columnNames;

        public UnitStatsTableModel() {
            // Init images.
            _icons = new ImageIcon[_unitClasses.size()];
            for (int index = 0; index < _unitClasses.size(); index++) {
                _icons[index] = new ImageIcon(UnitRenderer.getImage(_player.getId(), (Class) _unitClasses.get(index)));
            }

            // Init column names.
            _columnNames = new String[6];
            _columnNames[0] = "Unit Type";
            _columnNames[1] = "In Production";
            _columnNames[2] = "In Play";
            _columnNames[3] = "Enemy Killed";
            _columnNames[4] = "Lost to Enemy";
            _columnNames[5] = "Turns Till Next Done";
        }

        public void adjustColumnWidths(JTable table) {
            TableColumn column = table.getColumn(_columnNames[5]);
            JLabel label = new JLabel(_columnNames[5]);
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
            return _unitClasses.size();
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
            if (column == COLUMN_UNIT_TYPE) {
                return _icons[row];
            } else if (column == COLUMN_IN_PRODUCTION) {
                return new Integer(_player.getUnitsInProduction((Class)
                        _unitClasses.get(row)));
            } else if (column == COLUMN_IN_PLAY) {
                return new Integer(_player.getUnitsInPlay((Class)
                        _unitClasses.get(row)));
            } else if (column == COLUMN_KILLED) {
                return new Integer(_player.getUnitsKilled((Class)
                        _unitClasses.get(row)));
            } else if (column == COLUMN_LOST) {
                return new Integer(_player.getUnitsLost((Class)
                        _unitClasses.get(row)));
            } else if (column == COLUMN_TIME_TILL_NEXT_DONE) {
                return new Integer(_player.getTimeTillNextDone((Class)
                        _unitClasses.get(row)));
            }

            return null;
        }
    }
}
