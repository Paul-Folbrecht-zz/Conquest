package com.osi.util.ui.dialogs;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Dialog built to wrap a panel.  If the panel implements WrappablePanel,
 * it is notified of Ok and Cancel events.
 *
 * @author Paul Folbrecht
 */
public class PanelWrapperDialog extends JDialog {
    protected JPanel _panel;
    protected boolean _canceled = false;
    protected String[] _buttons;

    /**
     *
     */
    public PanelWrapperDialog(Frame frame, String title, boolean modal,
                              JPanel panel, String[] buttons) {
        super(frame, title, modal);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        _panel = panel;
        _buttons = buttons;

        init();
        pack();
    }

    /**
     *
     */
    public JPanel getPanel() {
        return _panel;
    }

    /**
     *
     */
    public boolean wasCanceled() {
        return _canceled;
    }

    /**
     *
     */
    protected void init() {
        getContentPane().setLayout(new BorderLayout());
        _panel.setBorder(BorderFactory.createRaisedBevelBorder());
        getContentPane().add(_panel, BorderLayout.CENTER);
        if (_buttons.length > 0) {
            getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
        }
    }

    /**
     *
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        for (int index = 0; index < _buttons.length; index++) {
            JButton button = new JButton(_buttons[index]);
            panel.add(button);
            if (_buttons[index].equals("Ok")) {
                button.addActionListener(new OkAction());
                getRootPane().setDefaultButton(button);
            } else if (_buttons[index].equals("Cancel")) {
                button.addActionListener(new CancelAction());
            }
        }

        return panel;
    }

    /**
     *
     */
    protected class OkAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (_panel instanceof Validatable) {
                if (((Validatable) _panel).validateData() == false) {
                    return;
                } else {
                    ((Validatable) _panel).onOk();
                }
            }

            setVisible(false);
        }
    }

    /**
     *
     */
    protected class CancelAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (_panel instanceof Validatable) {
                ((Validatable) _panel).onCancel();
            }

            _canceled = true;
            setVisible(false);
        }
    }
}
