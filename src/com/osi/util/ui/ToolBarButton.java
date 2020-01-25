package com.osi.util.ui;


import javax.swing.*;


/**
 *
 */
public class ToolBarButton extends JButton {
    /**
     *
     */
    public ToolBarButton(String label, ImageIcon icon, String tooltip) {
        super(icon);

        setToolTipText(tooltip);
        setVerticalTextPosition(AbstractButton.BOTTOM);
        setHorizontalTextPosition(AbstractButton.CENTER);
        setFocusPainted(false);
    }
}

