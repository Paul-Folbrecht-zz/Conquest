package com.osi.conquest.ui.dialogs;


import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.Player;

import javax.swing.*;
import java.awt.*;


/**
 * Simple panel to allow the user to enter a text message and choose a Player to send it to.
 *
 * @author Paul Folbrecht
 */
public class SendPlayerMessagePanel extends JPanel {
    protected JTextArea _text;
    protected JComboBox _playerSelection;
    protected Player[] _players;

    /**
     *
     */
    public SendPlayerMessagePanel(Player[] players) {
        short newIndex = 0;

        // We don't want the current player among the "send to" choices.
        _players = new Player[players.length - 1];
        for (int index = 0; index < players.length; index++) {
            if (players[index] != ConquestFactory.getController().getCurrentPlayer()) {
                _players[newIndex++] = players[index];
            }
        }

        init();
    }

    /**
     *
     */
    public Player getTargetPlayer() {
        return (Player) _playerSelection.getSelectedItem();
    }

    /**
     *
     */
    public String getText() {
        return _text.getText();
    }

    /**
     *
     */
    protected void init() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        add(createPlayerSelectionPanel(), BorderLayout.NORTH);
        add(createTextPanel(), BorderLayout.CENTER);
    }

    /**
     *
     */
    protected JPanel createPlayerSelectionPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Send To: "));
        _playerSelection = new JComboBox(_players);
        panel.add(_playerSelection);

        return panel;
    }

    /**
     *
     */
    protected JComponent createTextPanel() {
        JScrollPane scroller;

        _text = new JTextArea("", 10, 40);
        _text.setAutoscrolls(true);
        _text.setEditable(true);
        scroller = new JScrollPane(_text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.getViewport().setViewSize(new Dimension(300, 200));

        return scroller;
    }
}
