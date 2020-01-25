package com.osi.util.ui.panels;


import com.osi.util.Endpoint;
import com.osi.util.ui.NumericTextField;
import com.osi.util.ui.dialogs.Validatable;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;


/**
 * @author Paul Folbrecht
 */
public class TCPPanel extends JPanel implements Validatable {
    protected JTextField[] _tcpAddress;
    protected JTextField _port;
    protected JLabel _addressLabel;
    protected JLabel _portLabel;

    public TCPPanel() {
        init();
    }

    /**
     *
     */
    public Endpoint getEndpoint() throws Exception {
        StringBuffer address = new StringBuffer(20);
        Endpoint endpoint;

        for (int index = 0; index < 4; index++) {
            address.append(_tcpAddress[index].getText());
            if (index != 3) {
                address.append(".");
            }
        }

        endpoint = new Endpoint(InetAddress.getByName(address.toString()),
                Integer.parseInt(_port.getText()));

        return endpoint;
    }

    /**
     * Validatable implementation.
     */
    public void onAdded() {
    }

    /**
     *
     */
    public boolean validateData() {
        for (int index = 0; index < 4; index++) {
            if (_tcpAddress[index].getText().length() < 1) {
                JOptionPane.showMessageDialog(this, "Invalid TCP/IP address.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    /**
     *
     */
    public void setEnablement(boolean enable) {
        if (_tcpAddress != null) {
            for (int index = 0; index < 4; index++) {
                _tcpAddress[index].setEnabled(enable);
            }
            _port.setEnabled(enable);
            _addressLabel.setEnabled(enable);
            _portLabel.setEnabled(enable);
        }
    }

    /**
     *
     */
    public void setAddress(String[] address) {
        for (int index = 0; index < 4; index++) {
            _tcpAddress[index].setText(address[index]);
        }
    }

    /**
     *
     */
    public void onOk() {
    }

    /**
     *
     */
    public void onCancel() {
    }

    /**
     *
     */
    public void setPort(int port) {
        _port.setText("" + port);
    }

    /**
     *
     */
    protected void init() {
        setLayout(new GridLayout(1, 2));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Host Address"),
                BorderFactory.createRaisedBevelBorder()));
        add(createAddressPanel());
        add(createPortPanel());
        setEnabled(false);
    }

    /**
     *
     */
    protected JPanel createAddressPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        _addressLabel = new JLabel("Address: ");
        panel.add(_addressLabel);
        _tcpAddress = new NumericTextField[4];
        for (int index = 0; index < 4; index++) {
            _tcpAddress[index] = new NumericTextField(3);
            panel.add(_tcpAddress[index]);
            if (index != 3) {
                panel.add(new JLabel("."));
            }
        }

        return panel;
    }

    /**
     *
     */
    protected JPanel createPortPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        _port = new NumericTextField(4);
        _port.setText("2000");
        _portLabel = new JLabel("Port: ");
        panel.add(_portLabel);
        panel.add(_port);

        return panel;
    }
}
