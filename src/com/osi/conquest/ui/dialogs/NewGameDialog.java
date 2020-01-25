package com.osi.conquest.ui.dialogs;


import com.osi.conquest.domain.ConquestMap;
import com.osi.conquest.domain.Player;
import com.osi.conquest.ui.dialogs.newgame.MapPanel;
import com.osi.conquest.ui.dialogs.newgame.PlayersPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Paul Folbrecht
 */
public class NewGameDialog extends JDialog {
    protected PlayersPanel _playersPanel = new PlayersPanel();
    protected MapPanel _mapPanel = new MapPanel();
    protected boolean _canceled = false;

    /**
     *
     */
    public NewGameDialog(Frame frame) {
        super(frame, "New Game Setup", true);

        init();
        pack();
    }

    /**
     *
     */
    public ConquestMap getMap() {
        return _mapPanel.getMap();
    }

    /**
     *
     */
    public Player[] getPlayers() {
        return _playersPanel.getPlayers();
    }

    /**
     *
     */
    public boolean wasCanceled() {
        return _canceled;
    }

    /**
     *
     */
    protected void init() {
        JPanel panel = new JPanel();
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);

        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        tabPane.addTab("Players", new JScrollPane(_playersPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        tabPane.addTab("Map", new JScrollPane(_mapPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        panel.setLayout(new BorderLayout());
        panel.add(tabPane, BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        getContentPane().add(panel);
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
        button.addActionListener(new OkAction());

        panel.add(button);
        button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
                _canceled = true;
            }
        });
        panel.add(button);

        return panel;
    }

    /**
     *
     */
    protected class OkAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (_playersPanel.validateData() && _mapPanel.validateData()) {
                _mapPanel.onOk();
                _playersPanel.onOk();
                setVisible(false);
            }
        }
    }
}
