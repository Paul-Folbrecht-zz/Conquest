package com.osi.util.ui.panels;


import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author Paul Folbrecht
 */
public class InetAddressChooserPanel extends JPanel {
    protected JComboBox _combo;

    /**
     *
     */
    public InetAddressChooserPanel() {
        init();
    }

    /**
     *
     */
    public InetAddress getAddress() {
        return (InetAddress) _combo.getSelectedItem();
    }

    /**
     *
     */
    protected void init() {
        String text =
                "Your machine has more than one TCP/IP address.  " +
                        "Please choose the address that will identify your machine.";

        setLayout(new GridLayout(2, 1));
        add(new JLabel(text));
        add(createAddressCombo());
    }

    /**
     *
     */
    protected JComponent createAddressCombo() {
        try {
            _combo = new JComboBox(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()));
            return _combo;
        } catch (UnknownHostException e) {
            // This can't happen with the localhost.
            return null;
        }
    }
}
