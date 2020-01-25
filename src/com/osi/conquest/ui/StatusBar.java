package com.osi.conquest.ui;


import javax.swing.*;
import java.awt.*;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 *
 * @author
 * @version 1.0
 */
public class StatusBar extends JPanel {
    protected JLabel _label = new JLabel("Conquest");


    /**
     *
     */
    public StatusBar() {
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        setLayout(new FlowLayout(FlowLayout.LEFT));
        _label.setHorizontalAlignment(JLabel.LEFT);
        add(_label);
    }

    /**
     *
     */
    public void setText(String text) {
        _label.setText(text);
        validate();
    }
}
