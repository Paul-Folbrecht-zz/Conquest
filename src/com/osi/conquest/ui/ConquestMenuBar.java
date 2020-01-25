package com.osi.conquest.ui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;


/**
 * @author Paul Folbrecht
 */
public class ConquestMenuBar extends JMenuBar {
    protected MainWindow _parent;


    /**
     *
     */
    public ConquestMenuBar(MainWindow parent) {
        _parent = parent;
        init();
    }

    /**
     *
     */
    protected void init() {
        addFileMenu();
        addGameMenu();
        addCommandsMenu();
        addOrdersMenu();
        addReportsMenu();
        addModeMenu();
        addHelpMenu();
    }

    /**
     *
     */
    protected void addFileMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("File");
        menu.setMnemonic('F');
        menu.addMenuListener(_parent.getMenuListener("File"));
        add(menu);

        item = new JMenuItem("Open...", 'o');
        item.addActionListener(_parent.getActionListener("File.Open"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                InputEvent.CTRL_DOWN_MASK));
        menu.add(item);

        item = new JMenuItem("Save", 's');
        item.addActionListener(_parent.getActionListener("File.Save"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK));
        menu.add(item);

        item = new JMenuItem("Save As...", 'v');
        item.addActionListener(_parent.getActionListener("File.SaveAs"));
        menu.add(item);

        item = new JMenuItem("Save Map...", 'm');
        item.addActionListener(_parent.getActionListener("File.SaveMap"));
        menu.add(item);

        menu.add(new JSeparator());
        item = new JMenuItem("Options...", 'p');
        item.addActionListener(_parent.getActionListener("File.Options"));
        menu.add(item);

        menu.add(new JSeparator());
        item = new JMenuItem("Exit", 'x');
        item.addActionListener(_parent.getActionListener("File.Exit"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                InputEvent.CTRL_DOWN_MASK));
        menu.add(item);
    }

    /**
     *
     */
    protected void addGameMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("Game");
        menu.setMnemonic('G');
        menu.addMenuListener(_parent.getMenuListener("Game"));
        add(menu);

        item = new JMenuItem("New...", 'n');
        item.addActionListener(_parent.getActionListener("Game.New"));
        menu.add(item);

        item = new JMenuItem("Join Remote Game...", 'j');
        item.addActionListener(_parent.getActionListener("Game.JoinRemoteGame"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK));
        menu.add(item);

        item = new JMenuItem("Quit Game", 'q');
        item.addActionListener(_parent.getActionListener("Game.QuitGame"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        menu.add(item);

        item = new JMenuItem("Send Player Message...", 's');
        item.addActionListener(_parent.getActionListener("Game.SendPlayerMessage"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        menu.add(item);
    }

    /**
     *
     */
    protected void addCommandsMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("Commands");
        menu.setMnemonic('c');
        menu.addMenuListener(_parent.getMenuListener("Commands"));
        add(menu);

        item = new JMenuItem("End Turn", 'e');
        item.addActionListener(_parent.getActionListener("Commands.EndTurn"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));
        menu.add(item);

        item = new JMenuItem("Center", 'c');
        item.addActionListener(_parent.getActionListener("Commands.Center"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
        menu.add(item);

        item = new JMenuItem("Next Unit", 'n');
        item.addActionListener(_parent.getActionListener("Commands.NextUnit"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        menu.add(item);

        item = new JMenuItem("View Units", 'v');
        item.addActionListener(_parent.getActionListener("Commands.ViewUnits"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
        menu.add(item);

        item = new JMenuItem("Play Movement Reports", 'p');
        item.addActionListener(_parent.getActionListener("Commands.PlayMovementReports"));
        menu.add(item);
    }

    /**
     *
     */
    protected void addOrdersMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("Orders");
        menu.setMnemonic('o');
        menu.addMenuListener(_parent.getMenuListener("Orders"));
        add(menu);

        item = new JMenuItem("Skip Unit", 'k');
        item.addActionListener(_parent.getActionListener("Orders.SkipUnit"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0));
        menu.add(item);

        item = new JMenuItem("Sentry", 's');
        item.addActionListener(_parent.getActionListener("Orders.Sentry"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
        menu.add(item);

        item = new JMenuItem("Unload Units", 'u');
        item.addActionListener(_parent.getActionListener("Orders.Unload"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0));
        menu.add(item);

        item = new JMenuItem("Define Patrol", 'd');
        item.addActionListener(_parent.getActionListener("Orders.Patrol"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
        menu.add(item);

        item = new JMenuItem("Clear Orders", 'c');
        item.addActionListener(_parent.getActionListener("Orders.Clear"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
        menu.add(item);

        item = new JMenuItem("Go Home", 'g');
        item.addActionListener(_parent.getActionListener("Orders.GoHome"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
        menu.add(item);
    }

    /**
     *
     */
    protected void addReportsMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("Reports");
        menu.setMnemonic('r');
        menu.addMenuListener(_parent.getMenuListener("Reports"));
        add(menu);

        item = new JMenuItem("Units...", 'u');
        item.addActionListener(_parent.getActionListener("Reports.Units"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
                InputEvent.CTRL_DOWN_MASK));
        menu.add(item);

        item = new JMenuItem("Cities...", 'c');
        item.addActionListener(_parent.getActionListener("Reports.Cities"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                InputEvent.CTRL_DOWN_MASK));
        menu.add(item);
    }

    /**
     *
     */
    protected void addModeMenu() {
        JMenu menu;
        JMenuItem item;
        ButtonGroup group = new ButtonGroup();

        menu = new JMenu("Mode");
        menu.setMnemonic('m');
        menu.addMenuListener(_parent.getMenuListener("Mode"));
        add(menu);

        item = new JRadioButtonMenuItem("Normal");
        item.addActionListener(_parent.getActionListener("Mode.Normal"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        item.setSelected(true);
        group.add(item);
        menu.add(item);

        item = new JRadioButtonMenuItem("Production");
        item.addActionListener(_parent.getActionListener("Mode.Production"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        group.add(item);
        menu.add(item);
    }

    /**
     *
     */
    protected void addHelpMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("Help");
        menu.setMnemonic('v');
        add(menu);

        menu.add(new JSeparator());

        item = new JMenuItem("About", 'a');
        item.addActionListener(_parent.getActionListener("Help.About"));
        menu.add(item);
    }
}

