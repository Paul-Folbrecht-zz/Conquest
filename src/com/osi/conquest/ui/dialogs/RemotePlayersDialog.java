package com.osi.conquest.ui.dialogs;


import com.osi.conquest.ConquestException;
import com.osi.conquest.domain.Player;
import com.osi.conquest.remote.PlayerConnectListener;
import com.osi.conquest.remote.host.Host;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * @author Paul Folbrecht
 */
public class RemotePlayersDialog extends JDialog implements PlayerConnectListener {
    protected JList _list;
    protected Player[] _players;
    protected boolean _canceled = false;
    protected boolean _allPlayersConnected = false;
    protected JButton _startButton;

    /**
     *
     */
    public RemotePlayersDialog(Frame owner, Player[] players, boolean isNewGame)
            throws ConquestException {
        super(owner, "Waiting For Remote Players", true);

        _players = players;
        Host.getInstance().waitForPlayers(this, _players, isNewGame);

        init();
        pack();
    }

    /**
     *
     */
    public boolean wasCanceled() {
        return _canceled;
    }

    /**
     * PlayerConnectListener impl.  Called by the host whenever a remote player connects.
     * Updates the dialog's player list.
     */
    public void playerConnected(Player player, Player[] allPlayers, boolean allPlayersConnected) {
        synchronized (_players) {
            _players = allPlayers;
            _allPlayersConnected = allPlayersConnected;
        }
    }

    /**
     *
     */
    protected void init() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createPlayerListPanel(), BorderLayout.CENTER);
        getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);

        new Timer(250, new RefreshPlayersAction()).start();
    }

    /**
     *
     */
    protected JPanel createPlayerListPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        panel.add(new JLabel("Connected players:"), BorderLayout.NORTH);
        _list = new JList(new DefaultListModel());
        panel.add(new JScrollPane(_list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        return panel;
    }

    /**
     *
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton button;

        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());

        _startButton = new JButton("Start Game");
        _startButton.addActionListener(new StartGameAction());
        _startButton.setEnabled(false);
        panel.add(_startButton);

        button = new JButton("Cancel");
        button.addActionListener(new CancelAction());
        panel.add(button);

        return panel;
    }

    /**
     *
     */
    protected class RefreshPlayersAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            synchronized (_players) {
                Vector names = new Vector();

                for (int index = 0; index < _players.length; index++) {
                    if (_players[index] != null && _players[index].isConnected()) {
                        names.add(_players[index].getName());
                    }
                }

                _list.setListData(names);
            }

            _startButton.setEnabled(_allPlayersConnected);
        }
    }

    /**
     *
     */
    protected class StartGameAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            setVisible(false);
        }
    }

    /**
     *
     */
    protected class CancelAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            _canceled = true;
            setVisible(false);
        }
    }
}
