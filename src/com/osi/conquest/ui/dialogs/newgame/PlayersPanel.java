package com.osi.conquest.ui.dialogs.newgame;


import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.Player;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * @author Paul Folbrecht
 */
public class PlayersPanel extends JPanel {
    protected JSlider _numPlayers;
    protected PlayerSetupPanel[] _playerPanels;
    protected Player[] _players;

    /**
     *
     */
    public PlayersPanel() {
        init();
        loadPrefs();
    }

    /**
     *
     */
    public boolean validateData() {
        boolean haveLocalPlayer = false;

        for (int index = 0; index < getNumPlayers(); index++) {
            if (_playerPanels[index].validateData() == false) {
                return false;
            }

            if (_playerPanels[index].isPlayerLocal()) {
                haveLocalPlayer = true;
            }
        }

        if (!haveLocalPlayer) {
            JOptionPane.showMessageDialog(this,
                    "There must be at least one local player.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     *
     */
    public Player[] getPlayers() {
        if (_players == null) {
            _players = new Player[getNumPlayers()];
            for (int index = 0; index < getNumPlayers(); index++) {
                if (_playerPanels[index].isPlayerLocal()) {
                    // Any player created here is local to the host.
                    _players[index] =
                            ConquestFactory.createPlayer(index, _playerPanels[index].getPlayerName(), true);
                } else {
                    // Placeholder for remote player.  Player created when it's machine connects to the host.
                    _players[index] = null;
                }
            }
        }

        return _players;
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
        setLayout(new BorderLayout());
        add(createNumPlayersPanel(), BorderLayout.NORTH);
        add(createPlayerSetupPanel(), BorderLayout.CENTER);
    }

    /**
     *
     */
    protected JPanel createNumPlayersPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Number of Players"),
                BorderFactory.createRaisedBevelBorder()));
        _numPlayers = new JSlider(JSlider.HORIZONTAL, 2, Player.MAX_PLAYERS, 2);
        _numPlayers.addChangeListener(new NumPlayersHandler());
        _numPlayers.setMajorTickSpacing(1);
        _numPlayers.setMinorTickSpacing(1);
        _numPlayers.setSnapToTicks(true);
        _numPlayers.setPaintLabels(true);
        _numPlayers.setPaintTicks(true);
        _numPlayers.setValue(2);
        panel.add(_numPlayers);

        return panel;
    }

    /**
     *
     */
    protected JPanel createPlayerSetupPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(4, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Player Setup"),
                BorderFactory.createRaisedBevelBorder()));
        _playerPanels = new PlayerSetupPanel[Player.MAX_PLAYERS];
        for (int index = 0; index < Player.MAX_PLAYERS; index++) {
            _playerPanels[index] = new PlayerSetupPanel(index);
            _playerPanels[index].setEnabled(index < 2);
            panel.add(_playerPanels[index]);
        }

        return panel;
    }

    /**
     *
     */
    protected int getNumPlayers() {
        return _numPlayers.getValue();
    }

    /**
     *
     */
    protected void loadPrefs() {
        try {
            _numPlayers.setValue(PropertyManager.getIntPref("playersPanel.numPlayers"));
            for (int index = 0; index < _playerPanels.length; index++) {
                _playerPanels[index].loadPrefs();
            }
        } catch (ConquestRuntimeException e) {
            // Value not there- not a problem.
        }
    }

    /**
     *
     */
    protected void savePrefs() {
        PropertyManager.setPref("playersPanel.numPlayers", "" + _numPlayers.getValue());
        for (int index = 0; index < _playerPanels.length; index++) {
            _playerPanels[index].savePrefs();
        }
        PropertyManager.savePrefs();
    }

    /**
     *
     */
    protected class NumPlayersHandler implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            for (int index = 0; index < Player.MAX_PLAYERS; index++) {
                if (_playerPanels != null) {
                    _playerPanels[index].setEnabled(index < getNumPlayers());
                }
            }
        }
    }

    /**
     *
     */
    protected class PlayerSetupPanel extends JPanel {
        protected static final int HUMAN_LOCAL = 0;
        protected static final int HUMAN_REMOTE = 1;

        protected JComboBox _type;
        protected JLabel _nameLabel;
        protected JTextField _name;
        protected int _index;

        /**
         *
         */
        public PlayerSetupPanel(int index) {
            _index = index;
            init();
        }

        /**
         *
         */
        public void loadPrefs() throws ConquestRuntimeException {
            _type.setSelectedIndex(PropertyManager.getIntPref("playersPanel.type" + _index));
            _name.setText(PropertyManager.getPref("playersPanel.name" + _index));
        }

        /**
         *
         */
        public void savePrefs() {
            PropertyManager.setPref("playersPanel.type" + _index, "" + _type.getSelectedIndex());
            PropertyManager.setPref("playersPanel.name" + _index, _name.getText());
        }

        /**
         *
         */
        public boolean isPlayerLocal() {
            return (_type.getSelectedIndex() != HUMAN_REMOTE);
        }

        /**
         *
         */
        public String getPlayerName() {
            return _name.getText();
        }

        /**
         *
         */
        public boolean validateData() {
            if (isPlayerLocal() && _name.getText().length() < 1) {
                JOptionPane.showMessageDialog(this, "Please enter a name for player " + (_index + 1),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }

        /**
         *
         */
        public void setEnabled(boolean enable) {
            super.setEnabled(enable);

            _type.setEnabled(enable && _index != 0);
            _nameLabel.setEnabled(enable && isPlayerLocal());
            _name.setEnabled(enable && isPlayerLocal());
        }

        /**
         *
         */
        protected void init() {
            setBorder(BorderFactory.createTitledBorder("Player " +
                    (_index + 1)));
            setLayout(new BorderLayout());
            add(createTypePanel(), BorderLayout.CENTER);
        }

        /**
         *
         */
        protected JPanel createTypePanel() {
            JPanel panel = new JPanel();
            String[] types = {"Human - Local", "Human - Remote"};

            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(BorderFactory.createRaisedBevelBorder());
            _type = new JComboBox(types);
            _type.addItemListener(new TypeChangeHandler());
            _type.setSelectedIndex(0);
            // Force player1 to be local.
            _type.setEnabled(_index != 0);
            panel.add(_type);

            _nameLabel = new JLabel("Name: ");
            panel.add(_nameLabel);
            _name = new JTextField(12);
            panel.add(_name);

            return panel;
        }

        /**
         *
         */
        protected class TypeChangeHandler implements ItemListener {
            public void itemStateChanged(ItemEvent event) {
                setEnabled(true);
            }
        }
    }
}
