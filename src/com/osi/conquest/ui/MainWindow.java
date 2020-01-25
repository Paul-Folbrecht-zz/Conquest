package com.osi.conquest.ui;


import com.osi.conquest.ConquestException;
import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.*;
import com.osi.conquest.remote.client.ClientRemoteCommunicator;
import com.osi.conquest.ui.dialogs.*;
import com.osi.conquest.ui.dialogs.newgame.ConnectToHostPanel;
import com.osi.conquest.ui.event.GameListener;
import com.osi.conquest.ui.event.ModelChangeListener;
import com.osi.conquest.ui.panels.CityInfoPanel;
import com.osi.conquest.ui.panels.UnitInfoPanel;
import com.osi.conquest.ui.panels.UnitsPanel;
import com.osi.util.ui.dialogs.DialogUtils;
import com.osi.util.ui.dialogs.PanelWrapperDialog;
import com.osi.util.ui.panels.ThreadProgressPanel;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class MainWindow extends JFrame implements ModelChangeListener {
    public static final int STATE_NORMAL = 5;
    public static final int STATE_WAITING_FOR_HOST_CONNECT = 6;
    public static final int STATE_WAITING_FOR_CONNECTIONS = 7;
    public static final int STATE_WAITING_FOR_REMOTE_PLAYER = 8;
    public static final int STATE_PLAYING_MOVEMENT_REPORTS = 16;

    protected static MainWindow _instance;
    protected HashMap _menuListeners = new HashMap();
    protected HashMap _actionListeners = new HashMap();
    protected MainCanvas _canvas;
    protected WorldMapCanvas _worldMapCanvas;
    protected UnitInfoPanel _unitInfoPanel;
    protected CityInfoPanel _cityInfoPanel;
    protected OwnableObject _infoPanelObject;
    protected StatusBar _statusBar;
    protected ConquestMenuBar _menuBar;
    protected JToolBar _toolbar;
    protected Timer _timer;
    protected File _currentFile = null;
    protected File _currentDir = null;
    protected int _state = STATE_NORMAL;
    protected int _waitCount = 0;
    protected List _gameListeners = new ArrayList();
    protected List _modelChangeListeners = new ArrayList();
    protected City _cityPopupTarget = null;
    protected Unit _popupTarget;

    /**
     *
     */
    public MainWindow() {
        super("Conquest");
        // We're sure there's only going to be one of these.
        _instance = this;
        init();
    }

    /**
     *
     */
    public static MainWindow getInstance() {
        return _instance;
    }

    /**
     *
     */
    public void addGameListener(GameListener listener) {
        _gameListeners.add(listener);
    }

    /**
     *
     */
    public void addModelChangeListener(ModelChangeListener listener) {
        _modelChangeListeners.add(listener);
    }

    /**
     *
     */
    public void modelChanged(MapSquare square) {
        broadcastModelChanged(square);
    }

    /**
     *
     */
    public void modelChanged(MapSquare[] squares) {
        broadcastModelChanged(squares);
    }

    /**
     *
     */
    public int getState() {
        return _state;
    }

    /**
     *
     */
    public void waitCursorOn() {
        _waitCount++;

        if (_waitCount > 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
    }

    /**
     *
     */
    public void waitCursorOff() {
        _waitCount--;

        if (_waitCount <= 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     *
     */
    public void scrollTo(Point location) {
        getCanvas().scrollTo(location);
    }

    /**
     *
     */
    public MainCanvas getCanvas() {
        return _canvas;
    }

    /**
     *
     */
    public void updateUI(Rectangle rect) {
        broadcastUpdateUI(rect);
    }

    /**
     *
     */
    public void showCityDialog(Player player, City city, boolean readOnly) {
        MapSquare square = getController().getMap().getSquareAt(city.getLocation());
        CityDialog dlg = new CityDialog(this, player, city, readOnly);

        square.highlight();
        square.getRenderer().render(getCanvas().getBufferGraphics());
        updateUI(square.getArea());
        DialogUtils.showCentered(this, dlg);
        square.unHighlight();
        square.getRenderer().render(getCanvas().getBufferGraphics());
        updateUI(square.getArea());
    }

    /**
     *
     */
    public void setState(int state) {
        _state = state;
        updateStatusBar();
    }

    /**
     *
     */
    public void receiveRemoteGame(ConquestMap map, Player[] players) {
        if (getState() == STATE_WAITING_FOR_HOST_CONNECT) {
            try {
                setState(STATE_WAITING_FOR_REMOTE_PLAYER);
                waitCursorOn();
                getController().init(map, players);
                broadcastGameStarted();
                _currentFile = null;

                // Blank the canvas because there may be an instant where the current
                // player is set to a remote player- would allow this machine to see
                // that player's state.
                getCanvas().setBlankScreen();
            } finally {
                waitCursorOff();
            }
        } else {
            Logger.warn("Received InitGame message when not waiting.");
        }
    }

    /**
     *
     */
    public void initNewGame(ConquestMap map, Player[] players) {
        try {
            waitCursorOn();

            if (map.getSize() == null) {
                Thread thread = map.init();
                ThreadProgressPanel panel = new ThreadProgressPanel("Building map...", thread);
                PanelWrapperDialog dlg =
                        new PanelWrapperDialog(this, "Conquest", true, panel, new String[0]);
                DialogUtils.showCentered(this, dlg);
            }

            _currentFile = null;
            getController().init(map, players);
            getController().newGame();
            broadcastGameStarted();
            getController().activateLocalPlayer();
        } finally {
            waitCursorOff();
        }
    }

    /**
     *
     */
    public void initRestoredGame(ObjectInputStream stream) throws Exception {
        try {
            waitCursorOn();
            setState(((Integer) stream.readObject()).intValue());
            ConquestFactory.setController((GameController) stream.readObject());
            getController().loadMap();
            if (waitForRemotePlayers(getController().getPlayers(), false)) {
                getController().gameRestored();
                broadcastGameStarted();
                getCanvas().unsetBlankScreen();
            } else {
                endCurrentGame(false);
            }
        } finally {
            waitCursorOff();
        }
    }

    /**
     *
     */
    public void onPlayerActivated() {
        broadcastPlayerDeactivated();
        broadcastPlayerActivated();
    }

    /**
     *
     */
    public void onUnitOutOfGas(Unit unit) {
        JOptionPane.showMessageDialog(this,
                "Your air unit ran out of fuel and crashed.", "Alert",
                JOptionPane.WARNING_MESSAGE);
        onUnitDestroyed(unit);
    }

    /**
     *
     */
    public void onUnitDestroyed(Unit unit) {
        getCanvas().onUnitDestroyed(unit);
    }

    /**
     *
     */
    public void onCityTaken(City city) {
    }

    /**
     *
     */
    public void displayPlayerMessage(String from, String msg) {
        JOptionPane.showMessageDialog(this, msg, "Message from " + from, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *
     */
    public void showError(String msg, Exception e) {
        if (e != null) {
            Logger.error(e);
            msg += ": " + e.getClass() + ": " + e.getMessage();
        }
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     *
     */
    public MenuListener getMenuListener(String menu) {
        MenuListener listener = (MenuListener) _menuListeners.get(menu);

        if (listener == null) {
            throw new RuntimeException("getMenuListener(): Unknown menu: " + menu);
        }

        return listener;
    }

    /**
     *
     */
    public ActionListener getActionListener(String command) {
        ActionListener listener = (ActionListener) _actionListeners.get(command);

        if (listener == null) {
            throw new RuntimeException("getActionListener(): Unknown command: " + command);
        }

        return listener;
    }

    /**
     *
     */
    public boolean isGameInProgress() {
        return (
                getController() != null && getController().getCurrentPlayer() != null);
    }

    /**
     *
     */
    public boolean promptEndTurn() {
        return (JOptionPane.showConfirmDialog(this,
                "There are no units with movement left.  Do you want to end the turn?", "End Turn?",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }

    /**
     *
     */
    public boolean promptEndGame() {
        boolean end = true;

        if (isGameInProgress()) {
            String message = "Are you sure you wish to quit the current game?";

            if (JOptionPane.showConfirmDialog(this, message, "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                end = false;
            }
        }

        return end;
    }

    /**
     *
     */
    public boolean endCurrentGame(boolean initiatedByPlayer) {
        boolean end = true;

        if (isGameInProgress()) {
            if (initiatedByPlayer) {
                end = promptEndGame();
            }

            if (end) {
                if (initiatedByPlayer) {
                    getController().localPlayerQuitting();
                }
                setState(STATE_NORMAL);
                getController().endGame();
                broadcastGameEnded();
                ConquestFactory.setController(null);
                System.gc();
            }
        }

        return end;
    }

    /**
     * Sets a unit to be used as the target of a command sent via the unit context menu.  (Actually
     * there is no way to tell if the command was invoked through the context menu or the main menu,
     * but that doesn't matter because the popup target will only be non-null when a popup menu
     * exists.)
     */
    public void setPopupTarget(Unit target) {
        _popupTarget = target;
    }

    /**
     *
     */
    protected void setPopupTarget(City city) {
        _cityPopupTarget = city;
    }

    /**
     *
     */
    protected boolean isHost() {
        return (getController() != null && getController().isHost());
    }

    /**
     *
     */
    protected void init() {
        addWindowListener(new MainWindowListener());
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        initMenuListeners();
        initActionListeners();
        initStatusBar();
        initMenuBar();
        initToolbar();
        initCanvases();
        initTimer();

        loadPrefs();
        pack();

        loadStaticData();
    }

    /**
     *
     */
    protected void loadStaticData() {
        class LoadDataAction implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                Thread thread = new Thread(new DataLoader());
                ThreadProgressPanel panel = new ThreadProgressPanel("Initializing...", thread);
                PanelWrapperDialog dlg =
                        new PanelWrapperDialog(MainWindow.this, "Conquest", true, panel, new String[0]);
                DialogUtils.showCentered(MainWindow.this, dlg);
            }

            class DataLoader implements Runnable {
                public void run() {
                    // Create a dummy unit to force loading of static data.
                    ConquestFactory.createUnit("com.osi.conquest.domain.impl.unit.Infantry", null, null);
                }
            }
        }

        Timer timer = new Timer(1, new LoadDataAction());
        timer.setRepeats(false);
        timer.start();
    }

    /**
     *
     */
    protected void initStatusBar() {
        _statusBar = new StatusBar();
        getContentPane().add(_statusBar, BorderLayout.SOUTH);
    }

    /**
     *
     */
    protected void initMenuBar() {
        _menuBar = new ConquestMenuBar(this);
        getContentPane().add(_menuBar, BorderLayout.NORTH);
        setJMenuBar(_menuBar);
    }

    /**
     *
     */
    protected void initToolbar() {
        _toolbar = new JToolBar();
    }

    /**
     *
     */
    protected void initCanvases() {
        Dimension size = new Dimension((int) (getGraphicsConfiguration().getBounds().getWidth() * .75),
                (int) (getGraphicsConfiguration().getBounds().getHeight() * .75));
        JSplitPane rightSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane mainSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JScrollPane mainCanvasScroller = createDefaultScroller(MapSquare.WIDTH, MapSquare.HEIGHT);
        JScrollPane worldMapScroller = createDefaultScroller(2, 2);
        JScrollPane infoPanelScroller = createDefaultScroller(1, 1);
        JPanel infoPanel = new JPanel();

        _canvas = new MainCanvas(mainCanvasScroller.getViewport());
        _canvas.setSize(size);
        _worldMapCanvas = new WorldMapCanvas();
        addGameListener(_canvas);
        addGameListener(_worldMapCanvas);
        addModelChangeListener(_canvas);
        addModelChangeListener(_worldMapCanvas);
        _canvas.addDisplayListener(_worldMapCanvas);

        _unitInfoPanel = new UnitInfoPanel();
        _cityInfoPanel = new CityInfoPanel();
        infoPanel.add(_unitInfoPanel);
        infoPanel.add(_cityInfoPanel);
        _unitInfoPanel.setLocation(0, 0);
        _cityInfoPanel.setLocation(0, 0);
        infoPanelScroller.setViewportView(infoPanel);

        // Setup the right-pane component, which is also a JSplitPane.
        worldMapScroller.setViewportView(_worldMapCanvas);
        rightSplitter.setTopComponent(worldMapScroller);
        rightSplitter.setBottomComponent(infoPanelScroller);
        rightSplitter.setResizeWeight(.3);
        rightSplitter.setDividerLocation((int) (size.getHeight() / 2));

        // Setup the main splitter.
        mainCanvasScroller.setViewportView(_canvas);
        mainSplitter.setLeftComponent(mainCanvasScroller);
        mainSplitter.setRightComponent(rightSplitter);
        mainSplitter.setResizeWeight(.75);
        mainSplitter.setDividerLocation((int) (size.getWidth() * .75));

        getContentPane().add(mainSplitter, BorderLayout.CENTER);
    }

    /**
     *
     */
    protected JScrollPane createDefaultScroller(int xIncrement, int yIncrement) {
        JScrollPane scroller = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scroller.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        scroller.setViewportBorder(BorderFactory.createRaisedBevelBorder());
        scroller.getHorizontalScrollBar().setUnitIncrement(xIncrement);
        scroller.getVerticalScrollBar().setUnitIncrement(yIncrement);

        return scroller;
    }

    /**
     *
     */
    protected void initWorldMapCanvas() {
    }

    /**
     *
     */
    protected void initTimer() {
        _timer = new Timer(750, new TimerListener());
        _timer.start();
    }

    /**
     *
     */
    protected void initMenuListeners() {
        _menuListeners.put("File", new FileMenuListener());
        _menuListeners.put("Game", new GameMenuListener());
        _menuListeners.put("Commands", new CommandsMenuListener());
        _menuListeners.put("Orders", new OrdersMenuListener());
        _menuListeners.put("Reports", new ReportsMenuListener());
        _menuListeners.put("Mode", new ModeMenuListener());
    }

    /**
     *
     */
    protected void initActionListeners() {
        _actionListeners.put("File.Open", new OpenAction());
        _actionListeners.put("File.Save", new SaveAction());
        _actionListeners.put("File.SaveAs", new SaveAsAction());
        _actionListeners.put("File.SaveMap", new SaveMapAction());
        _actionListeners.put("File.Options", new OptionsAction());
        _actionListeners.put("File.Exit", new ExitAction());

        _actionListeners.put("City.Production", new CityProductionAction());
        _actionListeners.put("City.Units", new CityUnitsAction());
        _actionListeners.put("City.SetAutoforward", new SetAutoForwardAction());

        _actionListeners.put("Game.New", new NewGameAction());
        _actionListeners.put("Game.JoinRemoteGame", new JoinRemoteGameAction());
        _actionListeners.put("Game.QuitGame", new QuitGameAction());
        _actionListeners.put("Game.SendPlayerMessage", new SendPlayerMessageAction());

        _actionListeners.put("Commands.EndTurn", new EndTurnAction());
        _actionListeners.put("Commands.Center", new CenterUnitAction());
        _actionListeners.put("Commands.NextUnit", new NextUnitAction());
        _actionListeners.put("Commands.ViewUnits", new ViewUnitsAction());
        _actionListeners.put("Commands.PlayMovementReports", new PlayMovementReportsAction());
        _actionListeners.put("Orders.SkipUnit", new SkipUnitAction());
        _actionListeners.put("Orders.Sentry", new SentryAction());
        _actionListeners.put("Orders.Unload", new UnloadAction());
        _actionListeners.put("Orders.Patrol", new PatrolAction());
        _actionListeners.put("Orders.Clear", new ClearOrdersAction());
        _actionListeners.put("Orders.GoHome", new GoHomeAction());
        _actionListeners.put("Orders.Activate", new ActivateAction());
        _actionListeners.put("Orders.Cancel", new CancelAction());

        _actionListeners.put("Reports.Units", new UnitReportAction());
        _actionListeners.put("Reports.Cities", new CityReportAction());

        _actionListeners.put("Mode.Normal", new ModeNormalAction());
        _actionListeners.put("Mode.Production", new ModeProductionAction());

        _actionListeners.put("Help.About", new HelpAboutAction());
    }

    /**
     * Returns the unit to be used as the target of an invoked command.  If the command was invoked
     * through the unit context menu, the popup target will be non-null and will be used.  If not,
     * the selected unit is used.
     */
    protected Unit getCommandTarget() {
        if (_popupTarget != null) return _popupTarget;
        return getController().getSelectedUnit();
    }

    /**
     *
     */
    protected void updateToolbar() {
    }

    /**
     *
     */
    protected void updateStatusBar() {
        _statusBar.setText(getCanvas().getStatusBarText());
    }

    /**
     *
     */
    protected void updateInfoPanel() {
        OwnableObject object = getCanvas().getInfoPanelObject();

        if (object != _infoPanelObject) {
            _infoPanelObject = object;

            if (object == null) {
                _unitInfoPanel.setVisible(false);
                _unitInfoPanel.setUnit(null);
                _cityInfoPanel.setCity(null);
                _cityInfoPanel.setVisible(false);
            } else if (object instanceof City) {
                _unitInfoPanel.setVisible(false);
                _unitInfoPanel.setUnit(null);
                if (((City) object).getOwner() == getController().getCurrentPlayer()) {
                    _cityInfoPanel.setCity((City) object);
                    _cityInfoPanel.setVisible(true);
                }
            } else if (object instanceof Unit) {
                _cityInfoPanel.setVisible(false);
                _cityInfoPanel.setCity(null);
                if (((Unit) object).getOwner() == getController().getCurrentPlayer()) {
                    _unitInfoPanel.setUnit((Unit) object);
                    _unitInfoPanel.setVisible(true);
                }
            }
        }
    }

    /**
     *
     */
    protected JMenuItem getItem(JMenu menu, String text) {
        for (int index = 0; index < menu.getMenuComponentCount(); index++) {
            if (menu.getMenuComponent(index) instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) menu.getMenuComponent(index);

                if (item.getText().equalsIgnoreCase(text)) {
                    return item;
                }
            }
        }

        Logger.error("getItem(): item " + text + " not found in " + menu);
        return null;
    }

    /**
     *
     */
    protected boolean waitForRemotePlayers(Player[] players, boolean newGame) {
        try {
            if (getController().anyRemotePlayers(players)) {
                RemotePlayersDialog dlg = new RemotePlayersDialog(this, players, newGame);

                DialogUtils.showCentered(MainWindow.getInstance(), dlg);
                if (dlg.wasCanceled()) {
                    ConquestFactory.setController(null);
                }

                return !dlg.wasCanceled();
            }
        } catch (ConquestException e) {
            showError("Communications error", e);
        }

        return true;
    }

    /**
     *
     */
    protected void enableIfGameInProgress(JComponent component) {
        component.setEnabled(isGameInProgress() && getState() == STATE_NORMAL);
    }

    /**
     *
     */
    protected void enableIfHost(JComponent component) {
        component.setEnabled(isHost());
    }

    /**
     *
     */
    protected void enableIfLocalPlayer(JComponent component) {
        if (isGameInProgress()) {
            Player player = getController().getCurrentPlayer();
            component.setEnabled(isHost() && player != null && player.isLocalToHost());
        } else {
            component.setEnabled(false);
        }
    }

    /**
     *
     */
    protected void enableIfUnitSelected(JComponent component) {
        component.setEnabled(isGameInProgress() &&
                getController().getSelectedUnit() != null);
    }

    /**
     *
     */
    protected void updateViewUnits(JComponent component) {
        if (isGameInProgress() && getController().getSelectedUnit() != null &&
                getController().getSelectedUnit() instanceof Transport) {
            Transport transport = (Transport) getController().getSelectedUnit();
            component.setEnabled(transport.getUnitCount() > 0);
        } else {
            component.setEnabled(false);
        }
    }

    /**
     *
     */
    protected void updateClearOrders(JComponent component) {
        component.setEnabled(false);

        if (isGameInProgress() && getController().getSelectedUnit() != null) {
            Unit unit = getController().getSelectedUnit();
            if (unit.isOnPatrol() || unit.isOnSentry() || unit.hasMoveTo()) {
                component.setEnabled(true);
            }
        }
    }

    /**
     *
     */
    protected void updateUnloadUnits(JComponent component) {
        if (isGameInProgress() && getController().getSelectedUnit() != null &&
                getController().getSelectedUnit() instanceof Transport) {
            Transport transport = (Transport) getController().getSelectedUnit();
            component.setEnabled(transport.getFirstTransportedUnitWithMovement() != null);
        } else {
            component.setEnabled(false);
        }
    }

    /**
     *
     */
    protected void updatePlayMovementReports(JComponent component) {
        if (isGameInProgress() && getController().getCurrentPlayer() != null) {
            component.setEnabled(getController().getCurrentPlayer().hasMovementReports());
        } else {
            component.setEnabled(false);
        }
    }

    /**
     *
     */
    protected GameController getController() {
        return ConquestFactory.getController();
    }

    /**
     *
     */
    protected void saveAs() {
        JFileChooser chooser = new JFileChooser();

        if (_currentDir != null) {
            chooser.setCurrentDirectory(_currentDir);
        }
        if (_currentFile != null) {
            chooser.setSelectedFile(_currentFile);
        }

        if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
            _currentFile = chooser.getSelectedFile();
            _currentDir = chooser.getCurrentDirectory();
            saveToCurrentFile();
        }
    }

    /**
     *
     */
    protected void saveMap() {
        JFileChooser chooser = new JFileChooser();

        chooser.setCurrentDirectory(new File(PropertyManager.getMapPath()));
        if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
            saveMap(chooser.getSelectedFile().getName());
        }
    }

    /**
     *
     */
    protected void saveToCurrentFile() {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(_currentFile));

            waitCursorOn();
            stream.writeObject(new Integer(getState()));
            if (getController().getMapFilename().equals("")) {
                getController().setMapFilename(_currentFile.getName() + ".cmp");
                saveMap(_currentFile.getName() + ".cmp");
            }
            stream.writeObject(getController());
            stream.close();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "The file cannot be found; try 'Save As' instead", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            Logger.error(e);
            JOptionPane.showMessageDialog(this,
                    "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            waitCursorOff();
        }
    }

    /**
     *
     */
    protected void saveMap(String filename) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(PropertyManager.getMapPath() + filename));

            waitCursorOn();
            stream.writeObject(getController().getMap());
            stream.close();
            getController().setMapFilename(filename);
        } catch (Exception e) {
            Logger.error(e);
            JOptionPane.showMessageDialog(this,
                    "Error saving map file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            waitCursorOff();
        }
    }

    /**
     *
     */
    protected void exit() {
        savePrefs();
        System.exit(0);
    }

    /**
     *
     */
    protected void loadPrefs() {
        try {
            int x;
            int y;
            int width;
            int height;

            x = PropertyManager.getIntPref("mainWindow.bounds.x");
            y = PropertyManager.getIntPref("mainWindow.bounds.y");
            width = PropertyManager.getIntPref("mainWindow.bounds.width");
            height = PropertyManager.getIntPref("mainWindow.bounds.height");
            setBounds(x, y, width, height);
            getCanvas().setSize(width, height);
        } catch (ConquestRuntimeException e) {
            // Values don't exist- Ok.
        }
    }

    /**
     *
     */
    protected void savePrefs() {
        Rectangle bounds = getBounds();

        PropertyManager.setPref("mainWindow.bounds.x", "" + bounds.getLocation().x);
        PropertyManager.setPref("mainWindow.bounds.y", "" + bounds.getLocation().y);
        PropertyManager.setPref("mainWindow.bounds.width", "" + (int) bounds.getWidth());
        PropertyManager.setPref("mainWindow.bounds.height", "" + (int) bounds.getHeight());
        PropertyManager.savePrefs();
    }

    /**
     *
     */
    protected void broadcastModelChanged(MapSquare square) {
        for (int index = 0; index < _modelChangeListeners.size(); index++) {
            ((ModelChangeListener) _modelChangeListeners.get(index)).modelChanged(square);
        }
    }

    /**
     *
     */
    protected void broadcastModelChanged(MapSquare[] squares) {
        for (int index = 0; index < _modelChangeListeners.size(); index++) {
            ((ModelChangeListener) _modelChangeListeners.get(index)).modelChanged(squares);
        }
    }

    /**
     *
     */
    protected void broadcastUpdateUI(Rectangle area) {
        for (int index = 0; index < _modelChangeListeners.size(); index++) {
            ((ModelChangeListener) _modelChangeListeners.get(index)).updateUI(area);
        }
    }

    /**
     *
     */
    protected void broadcastGameStarted() {
        for (int index = 0; index < _gameListeners.size(); index++) {
            ((GameListener) _gameListeners.get(index)).onGameStarted();
        }
    }

    /**
     *
     */
    protected void broadcastGameEnded() {
        for (int index = 0; index < _gameListeners.size(); index++) {
            ((GameListener) _gameListeners.get(index)).onGameEnded();
        }
    }

    /**
     *
     */
    protected void broadcastPlayerActivated() {
        for (int index = 0; index < _gameListeners.size(); index++) {
            ((GameListener) _gameListeners.get(index)).onPlayerActivated();
        }
    }

    /**
     *
     */
    protected void broadcastPlayerDeactivated() {
        for (int index = 0; index < _gameListeners.size(); index++) {
            ((GameListener) _gameListeners.get(index)).onPlayerDeactivated();
        }
    }

    /**
     *
     */
    protected class MainWindowListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            if (endCurrentGame(true)) {
                exit();
            }
        }
    }

    /**
     *
     */
    protected class NewGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (endCurrentGame(true)) {
                NewGameDialog dlg = new NewGameDialog(MainWindow.this);

                ConquestFactory.createController(true);
                DialogUtils.showCentered(MainWindow.this, dlg);
                if (!dlg.wasCanceled()) {
                    if (waitForRemotePlayers(dlg.getPlayers(), true)) {
                        initNewGame(dlg.getMap(), dlg.getPlayers());
                    }
                } else {
                    ConquestFactory.setController(null);
                }
            }
        }
    }

    /**
     *
     */
    protected class JoinRemoteGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (endCurrentGame(true)) {
                try {
                    ConnectToHostPanel panel = new ConnectToHostPanel();
                    PanelWrapperDialog dlg = new PanelWrapperDialog(MainWindow.this, "Connect To Host", true, panel, new String[]{"Ok", "Cancel"});

                    DialogUtils.showCentered(MainWindow.this, dlg);
                    if (!dlg.wasCanceled()) {
                        setState(STATE_WAITING_FOR_HOST_CONNECT);
                        ConquestFactory.createController(false);
                        if (ClientRemoteCommunicator.getInstance().connectToHost(panel.getEndpoint(), panel.getName()) == false) {
                            String msg = "The host rejected your connection.\nIf you are restoring a saved game" +
                                    ", you must be sure to use the same player name as before.";
                            JOptionPane.showMessageDialog(MainWindow.this, msg, "Conquest", JOptionPane.INFORMATION_MESSAGE);
                            setState(STATE_NORMAL);
                        }
                    }
                } catch (Exception e) {
                    setState(STATE_NORMAL);
                    showError("Could not connect to host", e);
                }
            }
        }
    }

    /**
     *
     */
    protected class QuitGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            endCurrentGame(true);
        }
    }

    /**
     *
     */
    protected class SendPlayerMessageAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            SendPlayerMessagePanel panel = new SendPlayerMessagePanel(getController().getPlayers());
            PanelWrapperDialog dlg = new PanelWrapperDialog(MainWindow.this, "Send Player Message", true, panel, new String[]{"Ok", "Cancel"});

            DialogUtils.showCentered(MainWindow.this, dlg);
            if (!dlg.wasCanceled() && panel.getTargetPlayer() != null) {
                getController().sendPlayerMessage(getController().getCurrentPlayer().toString(), panel.getTargetPlayer(), panel.getText());
            }
        }
    }

    /**
     *
     */
    protected class OpenAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (endCurrentGame(true)) {
                JFileChooser chooser = new JFileChooser();

                if (_currentDir != null) {
                    chooser.setCurrentDirectory(_currentDir);
                }

                if (chooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        _currentFile = chooser.getSelectedFile();
                        _currentDir = chooser.getCurrentDirectory();
                        initRestoredGame(new ObjectInputStream(new FileInputStream(chooser.getSelectedFile())));
                    } catch (FileNotFoundException e) {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "The file cannot be found.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        Logger.error(e);
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Invalid file format.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     *
     */
    protected class SaveAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (_currentFile == null) {
                saveAs();
            } else {
                saveToCurrentFile();
            }
        }
    }

    /**
     *
     */
    protected class SaveAsAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            saveAs();
        }
    }

    /**
     *
     */
    protected class SaveMapAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            saveMap();
        }
    }

    /**
     *
     */
    protected class OptionsAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            //@todo
        }
    }

    /**
     *
     */
    protected class ExitAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (endCurrentGame(true)) {
                exit();
            }
        }
    }

    /**
     *
     */
    protected class EndTurnAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            boolean end = true;
            Unit unit = getController().getCurrentPlayer().getFirstUnitWithMovement();

            if (unit != null) {
                if (JOptionPane.showConfirmDialog(MainWindow.this,
                        "There are units with movement left; are you sure you wish to end the turn?",
                        "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    end = false;
                    getController().setSelectedUnit(unit);
                }
            }

            if (end) {
                getController().endOfTurn();
            }
        }
    }

    /**
     *
     */
    protected class CenterUnitAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getController().getSelectedUnit() != null) {
                getController().scrollTo(getController().getSelectedUnit().
                        getLocation());
            } else {
                City city = (City) getController().getCurrentPlayer().getCities().get(0);
                getController().scrollTo(city.getLocation());
            }
        }
    }

    /**
     *
     */
    protected class NextUnitAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            getController().nextUnit(null);
        }
    }

    /**
     *
     */
    protected class ViewUnitsAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            // Bring up the Units dlg.
            if (getCommandTarget() != null) {
                Transport transport = (Transport) getCommandTarget();
                UnitsPanel panel = new UnitsPanel(transport.getUnits(), true);
                PanelWrapperDialog dialog =
                        new PanelWrapperDialog(MainWindow.this, "Conquest", true, panel, new String[0]);
                DialogUtils.showCentered(MainWindow.this, dialog);
            }
        }
    }

    /**
     *
     */
    protected class PlayMovementReportsAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            class PlayReportsWorker implements Runnable {
                public void run() {
                    getController().getCurrentPlayer().playMovementReports();
                }
            }

            Thread thread = new Thread(new PlayReportsWorker());
            ThreadProgressPanel panel = new ThreadProgressPanel("Playing Movement Reports...", thread);
            PanelWrapperDialog dlg =
                    new PanelWrapperDialog(MainWindow.this, "Conquest", true, panel, new String[0]);
            DialogUtils.showCentered(MainWindow.this, dlg);
        }
    }

    /**
     *
     */
    protected class SkipUnitAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getCommandTarget() != null) {
                getCommandTarget().setMovementPoints(0);
                getController().nextUnit(null);
            }
        }
    }

    /**
     *
     */
    protected class SentryAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getCommandTarget() != null) {
                getCommandTarget().sentry();
                getCommandTarget().getRenderer().render(getCanvas().getBufferGraphics());
                getController().nextUnit(null);
            }
        }
    }

    /**
     *
     */
    protected class UnloadAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getCommandTarget() != null && getCommandTarget() instanceof Transport) {
                Transport transport = (Transport) getCommandTarget();
                Unit unit = transport.getFirstTransportedUnitWithMovement();

                transport.awakeUnits();
                getController().setSelectedUnit(unit);
            }
        }
    }

    /**
     *
     */
    protected class PatrolAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getCommandTarget() != null) {
                getController().setSelectedUnit(getCommandTarget());
                getCanvas().setPatrol();
            }
        }
    }

    /**
     *
     */
    protected class ClearOrdersAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getCommandTarget() != null) {
                getCommandTarget().clearOrders();
            }
        }
    }

    /**
     *
     */
    protected class GoHomeAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getCommandTarget() != null) {
                Unit unit = getCommandTarget();
                City city = getController().getMap().findClosestFriendlyCity(unit);

                if (city != null) {
                    UnitPath path = ConquestFactory.createUnitPath(unit.getLocation(), city.getLocation(), unit);

                    if (path.calculatePath(false)) {
                        unit.setMovePath(path);
                        if (unit.executeMoveTo()) {
                            getController().nextUnit(null);
                        }
                    } else {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Cannot find a path to a friendly city.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     *
     */
    protected class ActivateAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (getCommandTarget() != null) {
                getController().setSelectedUnit(getCommandTarget());
            }
        }
    }

    /**
     *
     */
    protected class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
        }
    }

    /**
     *
     */
    protected class UnitReportAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            UnitStatsDialog dlg = new UnitStatsDialog(MainWindow.this,
                    getController().getCurrentPlayer());
            DialogUtils.showCentered(MainWindow.this, dlg);
        }
    }

    /**
     *
     */
    protected class CityReportAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            CityStatsDialog dlg = new CityStatsDialog(MainWindow.this,
                    getController().getCurrentPlayer());
            DialogUtils.showCentered(MainWindow.this, dlg);
        }
    }

    /**
     *
     */
    protected class ModeNormalAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            getCanvas().setState(MainCanvas.STATE_NORMAL);
            getController().nextUnit(null);
        }
    }

    /**
     *
     */
    protected class ModeProductionAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            getController().setSelectedUnit(null);
            getCanvas().setState(MainCanvas.STATE_PRODUCTION);
        }
    }

    /**
     *
     */
    protected class HelpAboutAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            JOptionPane.showMessageDialog(MainWindow.this,
                    "Conquest V1.0.  Copyright 2000 by Object Specialists, Inc.  All rights reserved.",
                    "Conquest", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     *
     */
    protected class CityProductionAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (_cityPopupTarget != null) {
                MainWindow.this.showCityDialog(getController().getCurrentPlayer(), _cityPopupTarget, false);
            }
        }
    }

    /**
     *
     */
    protected class CityUnitsAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (_cityPopupTarget != null) {
                UnitsPanel panel = new UnitsPanel(_cityPopupTarget.getUnits(), true);
                PanelWrapperDialog dialog =
                        new PanelWrapperDialog(MainWindow.this, "Conquest", true, panel, new String[0]);
                MapSquare square = getController().getMap().getSquareAt(_cityPopupTarget.getLocation());

                square.highlight();
                square.getRenderer().render(getCanvas().getBufferGraphics());
                updateUI(square.getArea());
                DialogUtils.showCentered(MainWindow.this, dialog);
                square.unHighlight();
                square.getRenderer().render(getCanvas().getBufferGraphics());
                updateUI(square.getArea());
            }
        }
    }

    /**
     *
     */
    protected class SetAutoForwardAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            if (_cityPopupTarget != null) getCanvas().doAutoForward(_cityPopupTarget);
        }
    }

    /**
     *
     */
    protected class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            updateToolbar();
            updateStatusBar();
            updateInfoPanel();
        }
    }

    /**
     *
     */
    protected abstract class MenuAdaptor implements MenuListener {
        public void menuCanceled(MenuEvent event) {
        }

        public void menuDeselected(MenuEvent event) {
        }

        public void menuSelected(MenuEvent event) {
            updateItems((JMenu) event.getSource());
        }

        protected abstract void updateItems(JMenu menu);
    }

    /**
     *
     */
    protected class FileMenuListener extends MenuAdaptor {
        protected void updateItems(JMenu menu) {
            enableIfLocalPlayer(getItem(menu, "Save"));
            enableIfLocalPlayer(getItem(menu, "Save As..."));
            enableIfGameInProgress(getItem(menu, "Save Map..."));
        }
    }

    /**
     *
     */
    protected class GameMenuListener extends MenuAdaptor {
        protected void updateItems(JMenu menu) {
            enableIfGameInProgress(getItem(menu, "Quit Game"));
            getItem(menu, "Send Player Message...").setEnabled(isGameInProgress());
        }
    }

    /**
     *
     */
    protected class CommandsMenuListener extends MenuAdaptor {
        protected void updateItems(JMenu menu) {
            enableIfGameInProgress(getItem(menu, "End Turn"));
            enableIfGameInProgress(getItem(menu, "Center"));
            enableIfGameInProgress(getItem(menu, "Next Unit"));
            updateViewUnits(getItem(menu, "View Units"));
            updatePlayMovementReports(getItem(menu, "Play Movement Reports"));
        }
    }

    /**
     *
     */
    protected class OrdersMenuListener extends MenuAdaptor {
        protected void updateItems(JMenu menu) {
            enableIfUnitSelected(getItem(menu, "Skip Unit"));
            enableIfUnitSelected(getItem(menu, "Sentry"));
            enableIfUnitSelected(getItem(menu, "Define Patrol"));
            enableIfUnitSelected(getItem(menu, "Go Home"));
            updateClearOrders(getItem(menu, "Clear Orders"));
            updateUnloadUnits(getItem(menu, "Unload Units"));
        }
    }

    /**
     *
     */
    protected class ReportsMenuListener extends MenuAdaptor {
        protected void updateItems(JMenu menu) {
            enableIfGameInProgress(getItem(menu, "Units..."));
            enableIfGameInProgress(getItem(menu, "Cities..."));
        }
    }

    /**
     *
     */
    protected class ModeMenuListener extends MenuAdaptor {
        protected void updateItems(JMenu menu) {
            enableIfGameInProgress(getItem(menu, "Normal"));
            enableIfGameInProgress(getItem(menu, "Production"));
        }
    }
}
