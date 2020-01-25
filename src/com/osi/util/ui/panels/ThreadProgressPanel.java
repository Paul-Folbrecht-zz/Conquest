package com.osi.util.ui.panels;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Paul Folbrecht
 */
public class ThreadProgressPanel extends JPanel {
    protected String _message;
    protected Thread _thread;
    protected Timer _timer;
    protected JProgressBar _progressBar;

    /**
     *
     */
    public ThreadProgressPanel(String message, Thread thread) {
        _message = message;
        _thread = thread;
        thread.start();
        init();
    }

    /**
     *
     */
    protected void init() {
        _timer = new Timer(500, new ThreadListener());
        _timer.start();

        setBorder(BorderFactory.createRaisedBevelBorder());
        setLayout(new GridLayout(2, 0));
        add(new JLabel(_message));
        _progressBar = new JProgressBar(0, 100);
        add(_progressBar);
    }

    /**
     *
     */
    protected class ThreadListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            _progressBar.setValue(_progressBar.getValue() + 10);
            if (_progressBar.getValue() >= 100) {
                _progressBar.setValue(0);
            }

            if (_thread == null || !_thread.isAlive()) {
                _timer.stop();
                SwingUtilities.getAncestorOfClass(JDialog.class, ThreadProgressPanel.this).
                        setVisible(false);
            }
        }
    }
}
