package com.osi.conquest.ui;


import com.osi.conquest.ConquestException;
import com.osi.conquest.Logger;
import com.osi.conquest.domain.*;
import com.osi.conquest.ui.event.DisplayListener;
import com.osi.conquest.ui.event.GameListener;
import com.osi.conquest.ui.event.ModelChangeListener;
import com.osi.util.ImageUtils;
import com.osi.util.ui.DoubleBufferedCanvas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Paul Folbrecht
 */
public class MainCanvas extends DoubleBufferedCanvas implements GameListener, ModelChangeListener {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PRODUCTION = 1;
    public static final int STATE_SET_PATROL = 2;
    public static final int STATE_SET_AUTO_FORWARD = 3;

    // Bit constants- Flags.
    public static final int MOVE_IN_PROGRESS = 1;

    protected static Cursor _moveToCursor;
    protected static Cursor _noMoveToCursor;
    protected static Cursor _combatCursor;
    protected Timer _timer;
    protected RefreshWindowWorker _refreshWindowWorker = new RefreshWindowWorker();
    protected MapSquare _moveFromSquare = null;
    protected MapSquare _moveToSquare = null;
    protected MapSquare _squareUnderMouse = null;
    protected Unit _unitUnderMouse = null;
    protected Unit _autoForwardUnit = null;
    protected City _autoForwardCity = null;
    protected int _state = STATE_NORMAL;
    protected int _flags = 0;
    protected boolean _flashUnit = true;
    protected MainWindow _mainWindow;
    protected List _displayListeners = new ArrayList();

    /**
     *
     */
    static {
        Dimension size = new Dimension(MapSquare.WIDTH, MapSquare.HEIGHT);
        Point hotSpot = new Point(size.width / 2, size.height / 2);
        GraphicsConfiguration config = MainWindow.getInstance().getGraphicsConfiguration();

        // Init custom cursors.
        try {
            _moveToCursor = ImageUtils.createCursor(config, "moveToCursor", hotSpot, size);
            _noMoveToCursor = ImageUtils.createCursor(config, "noMoveToCursor", hotSpot, size);
            _combatCursor = ImageUtils.createCursor(config, "combatCursor", hotSpot, size);
        } catch (ConquestException e) {
            Logger.error("Couldn't create cursors", e);
        }
    }

    /**
     *
     */
    public MainCanvas(JViewport parent) {
        _mainWindow = MainWindow.getInstance();
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
        parent.addChangeListener(new ViewportChangeHandler());
    }

    /**
     *
     */
    public void addDisplayListener(DisplayListener listener) {
        _displayListeners.add(listener);
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
    public boolean isMoveInProgress() {
        return ((_flags & MOVE_IN_PROGRESS) != 0);
    }

    /**
     *
     */
    public Unit getUnitUnderMouse() {
        return _unitUnderMouse;
    }

    /**
     *
     */
    public City getCityUnderMouse() {
        if (_squareUnderMouse != null) return _squareUnderMouse.getCity();
        else return null;
    }

    /**
     *
     */
    public OwnableObject getInfoPanelObject() {
        if (getCityUnderMouse() != null) return getCityUnderMouse();
        else if (getUnitUnderMouse() != null) return getUnitUnderMouse();
        else if (MainWindow.getInstance().isGameInProgress()) return getController().getSelectedUnit();
        else return null;
    }

    /**
     *
     */
    public String getStatusBarText() {
        String text;

        if (_mainWindow.getState() == MainWindow.STATE_WAITING_FOR_CONNECTIONS) {
            text = "Waiting for players to connect...";
        } else if (_mainWindow.getState() == MainWindow.STATE_WAITING_FOR_HOST_CONNECT) {
            text = "Connecting to host...";
        } else if (_mainWindow.getState() == MainWindow.STATE_WAITING_FOR_REMOTE_PLAYER) {
            text = "Waiting for remote players...";
        } else if (MainWindow.getInstance().isGameInProgress() == false) {
            text = "No game in progress.";
        } else if (isMoveInProgress()) {
            text = getMoveStatusText();
        } else if (getState() == STATE_PRODUCTION) {
            text = "Production Mode";
        } else if (getState() == STATE_SET_PATROL) {
            text = getDefinePatrolStatusText();
        } else if (getState() == STATE_SET_AUTO_FORWARD) {
            text = "Click on the square to move new units to; right-click to abort.";
        } else {
            text = getNormalModeStatusText();
        }

        return text;
    }

    /**
     *
     */
    public void setPatrol() {
        // Turn on Patrol mode until the user aborts or finishes setting waypoints.
        setState(STATE_SET_PATROL);
    }

    /**
     *
     */
    public void doAutoForward(City city) {
        setState(STATE_SET_AUTO_FORWARD);
        _autoForwardCity = city;
        getController().deselectUnit();
    }

    /**
     *
     */
    public void scrollTo(Point point) {
        JViewport parent = (JViewport) getParent();
        Dimension size = parent.getSize();

        point = new Point(point);
        point.x *= MapSquare.WIDTH;
        point.y *= MapSquare.HEIGHT;

        if (!parent.getViewRect().contains(point)) {
            point.x = (int) (point.x - (size.getWidth() / 2));
            point.y = (int) (point.y - (size.getHeight() / 2));

            if (point.x < 0) {
                point.x = 0;
            } else if (point.x > (getSize().getWidth() - size.getWidth())) {
                point.x = (int) (getSize().getWidth() - size.getWidth());
            }
            if (point.y < 0) {
                point.y = 0;
            } else if (point.y > (getSize().getHeight() - size.getHeight())) {
                point.y = (int) (getSize().getHeight() - size.getHeight());
            }

            parent.setViewPosition(point);
        }
    }

    /**
     *
     */
    public void modelChanged(MapSquare square) {
        square.getRenderer().render(getBufferGraphics());
    }

    /**
     *
     */
    public void modelChanged(MapSquare[] squares) {
        Graphics2D graphics = getBufferGraphics();

        for (int index = 0; index < squares.length; index++) {
            squares[index].getRenderer().render(graphics);
        }
    }

    /**
     *
     */
    public void updateUI(Rectangle area) {
        addDirtyRect(toDisplay(area));
        draw();
    }

    /**
     *
     */
    public Rectangle toDisplay(Rectangle area) {
        return new Rectangle(area.x * MapSquare.WIDTH, area.y * MapSquare.HEIGHT,
                area.width * MapSquare.WIDTH, area.height * MapSquare.HEIGHT);
    }

    /**
     *
     */
    public void onPlayerDeactivated() {
        setBlankScreen();
    }

    /**
     *
     */
    public void onPlayerActivated() {
        javax.swing.JOptionPane.showMessageDialog(_mainWindow,
                getController().getCurrentPlayer().toString() + " , click to begin your turn.");
        setState(STATE_NORMAL);
        unsetBlankScreen();
    }

    /**
     *
     */
    public void onGameStarted() {
        _moveFromSquare = null;
        _moveToSquare = null;
        _squareUnderMouse = null;

        init(getController().getMap().getSizeInPixels());
        setState(STATE_NORMAL);
        renderAndDrawAll();

        _timer = new Timer(500, new FlashUnitAction());
        _timer.start();
    }

    /**
     *
     */
    public void onGameEnded() {
        if (_timer != null) {
            _timer.stop();
        }
        _timer = null;
        setBlankScreen();
    }

    /**
     *
     */
    public void onUnitDestroyed(Unit unit) {
        cutDanglingReferences(unit);
    }

    /**
     *
     */
    public void renderAndDrawAll() {
        Graphics2D bufferGraphics = getBufferGraphics();

        bufferGraphics.setClip(null);
        addDirtyRect(new Rectangle(getController().getMap().getSizeInPixels()));
        if (_state == STATE_PRODUCTION) {
            getController().getMap().paint(bufferGraphics, false);
            getController().paintProductionModeSymbols(bufferGraphics);
        } else {
            getController().getMap().paint(bufferGraphics, true);
        }

        draw();
    }

    /**
     *
     */
    protected void init(Dimension size) {
        super.init(size);
        _squareUnderMouse = getController().getMap().getSquareAt(new Point(0, 0));
    }

    /**
     *
     */
    protected void broadcastOnScrolled(Point point) {
        for (int index = 0; index < _displayListeners.size(); index++) {
            ((DisplayListener) _displayListeners.get(index)).onScrolled(point, getParent().getSize());
        }
    }

    /**
     *
     */
    protected String getNormalModeStatusText() {
        Player player = getController().getCurrentPlayer();
        String text;

        text = player + ", turn " + player.getCurrentTurn();
        if (_squareUnderMouse != null) {
            text += "; [" + _squareUnderMouse.getLocation().x + ", " + _squareUnderMouse.getLocation().y + "]";

            //@todo Debug code!
            Unit unit = _squareUnderMouse.getUnit();
            if (unit != null) text += " Unit: " + unit.getStatusText();
        }

        return text;
    }

    /**
     *
     */
    protected String getMoveStatusText() {
        String text = null;

        if (_moveFromSquare != null && _moveToSquare != null) {
            return "Range: " + getController().getMap().getRange(_moveFromSquare.getLocation(), _moveToSquare.getLocation());
        }

        return text;
    }

    /**
     *
     */
    protected String getDefinePatrolStatusText() {
        if (getController().getSelectedUnit().getPath() == null) {
            return "Click the first waypoint; right-click to cancel.";
        } else {
            return "Total length: " + getController().getSelectedUnit().getPath().getLength();
        }
    }

    /**
     *
     */
    protected boolean inHandicappedMode() {
        return (_mainWindow.getState() != MainWindow.STATE_NORMAL);
    }

    /**
     *
     */
    protected void cutDanglingReferences(Unit unit) {
        if (_unitUnderMouse == unit) _unitUnderMouse = null;
    }

    /**
     *
     */
    protected void setState(int mode) {
        int oldMode = _state;

        _state = mode;

        if (mode == STATE_PRODUCTION || oldMode == STATE_PRODUCTION) renderAndDrawAll();

        repaint();
        _mainWindow.updateStatusBar();
    }

    /**
     *
     */
    protected void setMoveCursor(Unit unit, Point location) {
        if (unit.canIntentionallyAttack(_moveToSquare.getLocation()) && unit.isAdjacentTo(location))
            _mainWindow.setCursor(_combatCursor);
        else if (unit.canMoveTo(_moveToSquare.getLocation())) _mainWindow.setCursor(_moveToCursor);
        else _mainWindow.setCursor(_noMoveToCursor);
    }

    /**
     *
     */
    protected JPopupMenu createUnitMenu(Unit unit) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;

        item = new JMenuItem("Skip Unit");
        item.addActionListener(_mainWindow.getActionListener("Orders.SkipUnit"));
        menu.add(item);

        if (!unit.isOnSentry()) {
            item = new JMenuItem("Sentry");
            item.addActionListener(_mainWindow.getActionListener("Orders.Sentry"));
            menu.add(item);
        }

        if (unit instanceof Transport) {
            Transport transport = (Transport) unit;
            if (transport.getFirstTransportedUnitWithMovement() != null) {
                item = new JMenuItem("Unload Units");
                item.addActionListener(_mainWindow.getActionListener("Orders.Unload"));
                menu.add(item);
            }

            if (transport.getUnitCount() > 0) {
                item = new JMenuItem("View Units");
                item.addActionListener(_mainWindow.getActionListener("Commands.ViewUnits"));
                menu.add(item);
            }
        }

        item = new JMenuItem("Define Patrol");
        item.addActionListener(_mainWindow.getActionListener("Orders.Patrol"));
        menu.add(item);

        if (unit.isOnPatrol() || unit.isOnSentry() || unit.hasMoveTo()) {
            item = new JMenuItem("Clear Orders");
            item.addActionListener(_mainWindow.getActionListener("Orders.Clear"));
            menu.add(item);
        }

        item = new JMenuItem("Go Home");
        item.addActionListener(_mainWindow.getActionListener("Orders.GoHome"));
        menu.add(item);

        item = new JMenuItem("Activate");
        item.addActionListener(_mainWindow.getActionListener("Orders.Activate"));
        menu.add(item);

        item = new JMenuItem("Cancel");
        item.addActionListener(_mainWindow.getActionListener("Orders.Cancel"));
        menu.add(item);

        return menu;
    }

    /**
     *
     */
    protected JPopupMenu createCityMenu(City city) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;

        item = new JMenuItem("Production");
        item.addActionListener(_mainWindow.getActionListener("City.Production"));
        menu.add(item);

        if (city.getUnits().size() > 0) {
            item = new JMenuItem("Units");
            item.addActionListener(_mainWindow.getActionListener("City.Units"));
            menu.add(item);
        }

        item = new JMenuItem("Set Autoforward");
        item.addActionListener(_mainWindow.getActionListener("City.SetAutoforward"));
        menu.add(item);

        return menu;
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
    protected class FlashUnitAction implements ActionListener {
        protected boolean _unitDisplayed = false;

        public void actionPerformed(ActionEvent event) {
            Unit unit = getController().getSelectedUnit();

            if (unit != null && _flashUnit) {
                // If the unit is currently displayed, erase it; if not, draw it.
                if (_unitDisplayed) {
                    MapSquare square = getController().getMap().getSquareAt(unit.getLocation());

                    if (unit.isInTransport(square) && square.getUnit() != null) square.getUnit().getRenderer().render(getBufferGraphics());
                    else if (square.getHiddenUnit() != null) square.getHiddenUnit().getRenderer().render(getBufferGraphics());
                    else square.getRenderer().render(getBufferGraphics(), false);
                } else unit.getRenderer().render(getBufferGraphics());

                addDirtyRect(toDisplay(unit.getArea()));
                draw();

                _unitDisplayed = !_unitDisplayed;
            }
        }
    }

    /**
     *
     */
    protected class RefreshWindowWorker implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            addDirtyRect(new Rectangle(new Point(0, 0), getSize()));
            draw();
        }
    }

    /**
     *
     */
    protected class MouseHandler extends MouseAdapter {
        public void mousePressed(MouseEvent event) {
            if (!MainWindow.getInstance().isGameInProgress() || inHandicappedMode()) return;

            if ((event.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                Unit unit = getController().getSelectedUnit();

                // If this is AutoForward select mode, create a unit of the type the city in question is currently producing, to use for determining
                // valid moves.
                if (getState() == STATE_SET_AUTO_FORWARD) {
                    getController().deselectUnit();
                    unit = _autoForwardUnit = _autoForwardCity.getProductionRepresentation();
                }

                if (unit != null) {
                    _flags = _flags | MOVE_IN_PROGRESS;
                    _moveFromSquare = getController().getMap().getSquareAt(unit.getLocation());
                    _moveToSquare = getController().getMap().getSquareAtPixel(event.getPoint());
                    setMoveCursor(unit, _moveToSquare.getLocation());
                }

                _mainWindow.updateStatusBar();
            }
        }

        public void mouseReleased(MouseEvent event) {
            if (!MainWindow.getInstance().isGameInProgress() || inHandicappedMode()) return;

            if ((event.getModifiers() & InputEvent.BUTTON2_MASK) != 0) rightButtonUp(event);
            else if ((event.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                MapSquare square = getController().getMap().getSquareAtPixel(event.getPoint());

                if (isMoveInProgress()) _mainWindow.setCursor(Cursor.getDefaultCursor());

                if (square != null) {
                    if (getState() == STATE_SET_PATROL) mouseUpInPatrolSetup(square);
                    else if (getState() == STATE_SET_AUTO_FORWARD) mouseUpInAutoForwardSetup(square);
                    else if (getState() == STATE_NORMAL) mouseUp(square);
                }
            }

            _flags = (_flags & ~MOVE_IN_PROGRESS);
            _moveFromSquare = _moveToSquare = null;
            _mainWindow.updateStatusBar();
        }

        protected void rightButtonUp(MouseEvent event) {
            _mainWindow.setCursor(Cursor.getDefaultCursor());

            // Right-click cancels MoveTo or Patrol.
            if (isMoveInProgress()) {
                _flags = (_flags & ~MOVE_IN_PROGRESS);
                _moveFromSquare = _moveToSquare = null;
            } else if (getState() == STATE_SET_PATROL) {
                Unit unit = getController().getSelectedUnit();

                setState(STATE_NORMAL);
                if (unit != null && unit.getPath() != null) {
                    unit.getPath().erase(getBufferGraphics());
                    addDirtyRect(unit.getPath().getBoundingRect());
                    draw();
                    unit.setMovePath(null);
                }
            }

            doPopup(event);
        }

        protected void mouseUp(MapSquare square) {
            Unit unit = getController().getSelectedUnit();
            boolean unitDead = false;

            if (unit == null || square.getLocation().equals(unit.getLocation()) || !isMoveInProgress()) return;

            if (unit.isAdjacentTo(square.getLocation())) {
                if (unit.canAttack(square.getLocation())) unitDead = !getController().combat(getBufferGraphics(), unit, square.getLocation());

                // Move whether or not we just attacked something.
                if (unit.canMoveTo(square.getLocation()) && !unitDead) handleAdjacentMove(unit, square);
            } else if (unit.canMoveTo(square.getLocation())) handleMoveTo(unit, square);

            if (unit.getMovementPoints() <= 0 || unitDead) getController().nextUnit(null);
        }

        protected void handleAdjacentMove(Unit unit, MapSquare square) {
            Unit.MoveToReturn result = unit.moveOneSquare(square.getLocation());

            if (result.getDetail() == Unit.MoveToReturn.FAILED_FULL_TRANSPORT) {
                JOptionPane.showMessageDialog(
                        _mainWindow,
                        "There's not enough room on the transport.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        protected void handleMoveTo(Unit unit, MapSquare square) {
            UnitPath path = ConquestFactory.createUnitPath(unit.getLocation(), square.getLocation(), unit);

            if (path.calculatePath(true)) {
                unit.setMovePath(path);
                unit.executeMoveTo();
            }
            // First try failed - try it allowing squares occupied with a friendly unit in the path (they
            // hopefully won't still be there when the unit gets there).
            else if (path.calculatePath(false)) {
                Logger.info("MainCanvas.handleMoveTo(): calculatePath( false ) succeeded.");
                unit.setMovePath(path);
                unit.executeMoveTo();
            } else {
                JOptionPane.showMessageDialog(_mainWindow, "Cannot find a path to that location.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        protected void mouseUpInPatrolSetup(MapSquare square) {
            Unit unit = getController().getSelectedUnit();
            Point from;
            UnitPath path;

            // Add a path from the last square added to the current square
            // to the unit's path.
            if (!unit.hasMoveTo()) from = unit.getLocation();
            else from = unit.getPath().getLastSquare().getLocation();

            path = ConquestFactory.createUnitPath(from, square.getLocation(), unit);
            if (path.calculatePath(false)) {
                if (!unit.hasMoveTo()) unit.setMovePath(path);
                else unit.getPath().add(path);
            } else JOptionPane.showMessageDialog(_mainWindow, "Cannot find a path to that location.", "Error", JOptionPane.ERROR_MESSAGE);

            // Check if this square is the unit's current square- if so, the Patrol is
            // complete.
            if (square.getLocation().equals(unit.getLocation())) {
                setState(STATE_NORMAL);
                if (unit.hasMoveTo()) {
                    unit.onPatrol();
                    unit.executeMoveTo();
                }
                unit.getPath().erase(getBufferGraphics());
                addDirtyRect(unit.getPath().getBoundingRect());
                draw();
            } else if (unit.hasMoveTo()) {
                unit.getPath().paint(getBufferGraphics());
                addDirtyRect(unit.getPath().getBoundingRect());
                draw();
            }
        }

        protected void mouseUpInAutoForwardSetup(MapSquare square) {
            // Make a UnitPath from the autoforward city to the clicked square.
            // If it's legal, set it as the city's autoforward path.
            UnitPath path = ConquestFactory.createUnitPath(_autoForwardCity.getLocation(), square.getLocation(), _autoForwardUnit);

            if (path.calculatePath(false)) _autoForwardCity.setAutoForwardPath(path);
            else JOptionPane.showMessageDialog(_mainWindow,
                        "Cannot find a path to that location.", "Error", JOptionPane.ERROR_MESSAGE);

            _autoForwardCity = null;
            _autoForwardUnit = null;
            setState(STATE_NORMAL);
            getController().nextUnit(null);
        }

        public void mouseClicked(MouseEvent event) {
            eraseOldMovePath();

            if (event.getClickCount() == 2) {
                if (getController() != null) {
                    MapSquare square = getController().getMap().getSquareAtPixel(event.getPoint());
                    Player player = getController().getCurrentPlayer();

                    if (square != null) {
                        if (square.getUnit() != null && getState() == STATE_NORMAL) {
                            if (square.getUnit().getOwner() == player) getController().setSelectedUnit(square.getUnit());
                        } else if (square.getCity() != null &&
                                square.getCity().getOwner() == player && !inHandicappedMode()) {
                            _mainWindow.showCityDialog(player, square.getCity(), false);
                        }
                    }
                }
            }
        }

        protected void doPopup(MouseEvent event) {
            MapSquare square = getController().getMap().getSquareAtPixel(event.getPoint());

            eraseOldMovePath();

            if (square.getUnit() != null && square.getUnit().getOwner() == getController().getCurrentPlayer()) {
                MainWindow.getInstance().setPopupTarget(square.getUnit());
                showPopup(createUnitMenu(square.getUnit()), event.getPoint());
            } else if (square.getCity() != null && square.getCity().getOwner() == getController().getCurrentPlayer()) {
                _mainWindow.setPopupTarget(square.getCity());
                showPopup(createCityMenu(square.getCity()), event.getPoint());
            }
        }

        protected void eraseOldMovePath() {
            if (_unitUnderMouse != null && _unitUnderMouse.hasMoveTo()) {
                // Must erase the path that would have been drawn before.
                _unitUnderMouse.getPath().erase(getBufferGraphics());
                addDirtyRect(_unitUnderMouse.getPath().getBoundingRect());
                draw();
            }
        }

        protected void showPopup(JPopupMenu menu, Point point) {
            menu.addPopupMenuListener(new PopupMenuHandler());
            menu.show(MainCanvas.this, point.x, point.y);
        }
    }

    /**
     *
     */
    protected class PopupMenuHandler implements PopupMenuListener {
        public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            _flashUnit = false;
            if (getController().getSelectedUnit() != null) {
                getController().getSelectedUnit().getRenderer().render(getBufferGraphics());
                addDirtyRect(toDisplay(getController().getSelectedUnit().getArea()));
                draw();
            }
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
            class ResetPopupTargetWorker implements Runnable {
                public void run() {
                    MainWindow.getInstance().setPopupTarget((Unit) null);
                }
            }

            _flashUnit = true;
            // This method gets called *before* the menu item's action listener is invoked, so we can't immediately reset the popup target.
            SwingUtilities.invokeLater(new ResetPopupTargetWorker());
        }

        public void popupMenuCanceled(PopupMenuEvent event) {
            _flashUnit = true;
            MainWindow.getInstance().setPopupTarget((Unit) null);
        }
    }

    /**
     *
     */
    protected class MouseMotionHandler extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent event) {
            if (!MainWindow.getInstance().isGameInProgress()) return;

            _squareUnderMouse = getController().getMap().getSquareAtPixel(event.getPoint());
            eraseOldMovePath();

            _unitUnderMouse = null;
            if (_squareUnderMouse != null) {
                if (_squareUnderMouse.getUnit() != null && _squareUnderMouse.
                        getUnit().getOwner() == getController().getCurrentPlayer()) {
                    _unitUnderMouse = _squareUnderMouse.getUnit();
                } else if (_squareUnderMouse.getCity() != null && _squareUnderMouse.
                        getCity().getOwner() == getController().getCurrentPlayer()) {
                    _unitUnderMouse = _squareUnderMouse.getCity().getProductionRepresentation();
                }
            }

            if (_unitUnderMouse != null && _unitUnderMouse.hasMoveTo()) {
                _unitUnderMouse.getPath().paint(getBufferGraphics());
                addDirtyRect(_unitUnderMouse.getPath().getBoundingRect());
                draw();
            }

            _mainWindow.updateStatusBar();
        }

        public void mouseDragged(MouseEvent event) {
            if (!MainWindow.getInstance().isGameInProgress()) return;

            MapSquare square = getController().getMap().getSquareAtPixel(event.getPoint());
            if (square != null) {
                if ((event.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && isMoveInProgress()) {
                    handleMoveInProgress(square);
                }
            }

            _mainWindow.updateStatusBar();
        }

        protected void eraseOldMovePath() {
            if (_squareUnderMouse == null || _unitUnderMouse != _squareUnderMouse.getUnit()) {
                if (_unitUnderMouse != null && _unitUnderMouse.hasMoveTo()) {
                    // Must erase the path that would have been drawn before.
                    _unitUnderMouse.getPath().erase(getBufferGraphics());
                    addDirtyRect(_unitUnderMouse.getPath().getBoundingRect());
                    draw();
                }
            }
        }

        protected void handleMoveInProgress(MapSquare square) {
            Unit unit = getController().getSelectedUnit();

            if (getState() == STATE_SET_AUTO_FORWARD) unit = _autoForwardUnit;

            _moveToSquare = square;
            setMoveCursor(unit, _moveToSquare.getLocation());
        }
    }

    /**
     *
     */
    protected class ComponentHandler extends ComponentAdapter {
        public void componentResized(ComponentEvent event) {
            broadcastOnScrolled(((JViewport) getParent()).getViewPosition());
        }

        public void componentMoved(ComponentEvent event) {
            broadcastOnScrolled(((JViewport) getParent()).getViewPosition());
        }
    }

    /**
     *
     */
    protected class ViewportChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            broadcastOnScrolled(((JViewport) getParent()).getViewPosition());
            if (!_blankScreen) {
                // Do a redraw after any scroll- there seems to be some painting bugs in Swing regarding
                // JViewport scrolling.
                Timer timer = new Timer(1, _refreshWindowWorker);
                timer.setRepeats(false);
                timer.start();
            }
        }
    }
}
