package com.osi.util.ui.panels;


import javax.swing.*;
import java.awt.*;


/**
 * @author Paul Folbrecht
 */
public class GetNamePanel extends JPanel {
    protected JTextField _name;

    /**
     *
     */
    public GetNamePanel() {
        init();
    }

    /**
     *
     */
    public String getName() {
        return _name.getText();
    }

    /**
     *
     */
    public void setName(String name) {
        _name.setText(name);
    }

    /**
     *
     */
    protected void init() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Player Name"),
                BorderFactory.createRaisedBevelBorder()));
        add(new JLabel("Name: "));
        _name = new JTextField(20);
        add(_name);
    }
}
