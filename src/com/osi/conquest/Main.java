package com.osi.conquest;


import com.osi.conquest.ui.MainWindow;


/**
 * @author Paul Folbrecht
 */

public class Main {
    protected static String[] _args;

    /**
     *
     */
    public static void main(String[] args) {
        MainWindow frame;

        _args = args;
        validateArgs();

        frame = new MainWindow();
        frame.pack();
        frame.setVisible(true);
        frame.setSize(800, 600);
        frame.validate();
    }

    /**
     *
     */
    public static String getHomeDirectory() {
        return _args[0];
    }

    /**
     *
     */
    protected static void validateArgs() {
        if (_args.length != 1) {
            System.out.println("Usage: com.osi.conquest.Main <propertyFile>");
            System.exit(0);
        }
    }
}
