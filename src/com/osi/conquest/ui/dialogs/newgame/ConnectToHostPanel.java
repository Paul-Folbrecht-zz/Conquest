package com.osi.conquest.ui.dialogs.newgame;


import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.util.Endpoint;
import com.osi.util.ui.dialogs.Validatable;
import com.osi.util.ui.panels.GetNamePanel;
import com.osi.util.ui.panels.TCPPanel;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;


/**
 * @author Paul Folbrecht
 */
public class ConnectToHostPanel extends JPanel implements Validatable {
    protected TCPPanel _tcpPanel;
    protected GetNamePanel _namePanel;

    /**
     *
     */
    public ConnectToHostPanel() {
        init();
        loadPrefs();
    }

    /**
     *
     */
    public Endpoint getEndpoint() throws Exception {
        return _tcpPanel.getEndpoint();
    }

    /**
     *
     */
    public String getName() {
        return _namePanel.getName();
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
        if (_namePanel.getName().length() < 1) {
            JOptionPane.showMessageDialog(this, "Please enter a name.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (!_tcpPanel.validateData()) {
            return false;
        }

        return true;
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
    public void onCancel() {
    }

    /**
     *
     */
    protected void init() {
        setLayout(new GridLayout(2, 1));
        setBorder(BorderFactory.createRaisedBevelBorder());
        _tcpPanel = new TCPPanel();
        add(_tcpPanel);
        _namePanel = new GetNamePanel();
        add(_namePanel);
    }

    /**
     *
     */
    protected void loadPrefs() {
        try {
            int index = 0;
            String[] address = new String[4];
            StringTokenizer tokenizer =
                    new StringTokenizer(PropertyManager.getPref("connectToHost.hostIP"), ".");

            while (tokenizer.hasMoreTokens()) {
                address[index++] = tokenizer.nextToken();
            }
            _tcpPanel.setAddress(address);

            _tcpPanel.setPort(PropertyManager.getIntPref("connectToHost.port"));
            _namePanel.setName(PropertyManager.getPref("connectToHost.name"));
        } catch (ConquestRuntimeException e) {
            // Data not present in propfie.
        }
    }

    /**
     *
     */
    protected void savePrefs() {
        try {
            Endpoint endpoint = _tcpPanel.getEndpoint();

            PropertyManager.setPref("connectToHost.hostIP", endpoint.getAddress().getHostAddress());
            PropertyManager.setPref("connectToHost.port", "" + endpoint.getPort());
            PropertyManager.setPref("connectToHost.name", _namePanel.getName());
            PropertyManager.savePrefs();
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
