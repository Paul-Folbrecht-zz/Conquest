package com.osi.conquest.ui;


import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.ConquestMap;
import com.osi.conquest.domain.GameController;
import com.osi.conquest.domain.MapSquare;
import com.osi.conquest.ui.event.DisplayListener;
import com.osi.conquest.ui.event.GameListener;
import com.osi.conquest.ui.event.ModelChangeListener;
import com.osi.conquest.ui.renderers.MapSquareRenderer;
import com.osi.util.ui.DoubleBufferedCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * @author Paul Folbrecht
 */
public class WorldMapCanvas extends DoubleBufferedCanvas implements
        GameListener, DisplayListener, ModelChangeListener {
    private Rectangle _areaMarker = new Rectangle(0, 0, 0, 0);
    private boolean initialized = false;

    /**
     *
     */
    public WorldMapCanvas() {
        addMouseListener(new MouseHandler());
    }

    /**
     *
     */
    public void modelChanged(MapSquare square) {
        square.getRenderer().renderThumbnail(getBufferGraphics());
    }

    /**
     *
     */
    public void modelChanged(MapSquare[] squares) {
        Graphics2D graphics = getBufferGraphics();

        for (int index = 0; index < squares.length; index++) {
            modelChanged(squares[index]);
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
        return new Rectangle(area.x * MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH,
                area.y * MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT,
                area.width * MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH,
                area.height * MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT);
    }

    /**
     *
     */
    public void renderAndDrawAll() {
        if (MainWindow.getInstance().isGameInProgress()) {
            Dimension size = getController().getMap().getSize();

            renderSquares(new Point(0, 0), new Point((int) size.getWidth(), (int) size.getHeight()), false);
            addDirtyRect(new Rectangle(new Point(0, 0), getWorldMapSize()));
            draw();
        }

        initialized = true;
    }

    /**
     *
     */
    public void renderAndDrawCities() {
        if (MainWindow.getInstance().isGameInProgress()) {
            Dimension size = getController().getMap().getSize();

            renderSquares(new Point(0, 0), new Point((int) size.getWidth(), (int) size.getHeight()), true);
            addDirtyRect(new Rectangle(new Point(0, 0), getWorldMapSize()));
            draw();
        }
    }

    /**
     * Overridden to optimize redrawing.  Since this view doesn't display units, only cities need
     * to be redrawn.
     */
    public void unsetBlankScreen() {
        _blankScreen = false;
        if (initialized && !getController().getMap().getParameters().terrainHidden) {
            renderAndDrawCities();
        } else {
            renderAndDrawAll();
        }
    }

    /**
     *
     */
    public Dimension getWorldMapSize() {
        Dimension mapSize = getController().getMap().getSize();
        return new Dimension((int) mapSize.getWidth() * MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH,
                (int) mapSize.getHeight() * MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT);
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
        unsetBlankScreen();
    }

    /**
     *
     */
    public void onGameStarted() {
        init(getWorldMapSize());
        ((JViewport) getParent()).setPreferredSize(getWorldMapSize());
        setVisible(true);
    }

    /**
     *
     */
    public void onGameEnded() {
        initialized = false;
        setBlankScreen();
    }

    /**
     *
     */
    public void onScrolled(Point point, Dimension size) {
        if (MainWindow.getInstance().isGameInProgress()) {
            eraseAreaMarker();
            _areaMarker.setLocation(convertMainCanvasPoint(point));
            _areaMarker.setSize(convertMainCanvasDimension(size));
            renderAreaMarker();
            draw();
        }
    }

    /**
     *
     */
    protected void renderSquares(Point topLeft, Point bottomRight, boolean onlyCities) {
        Graphics2D graphics = getBufferGraphics();
        ConquestMap map = getController().getMap();

        graphics.setStroke(new BasicStroke(1));

        for (int x = topLeft.x; x < bottomRight.x; x++) {
            for (int y = topLeft.y; y < bottomRight.y; y++) {
                MapSquare square = map.fastGetSquareAt(x, y);

                if (!onlyCities || square.getCity() != null) {
                    square.getRenderer().renderThumbnail(graphics);
                }
            }
        }
    }

    /**
     *
     */
    protected void eraseAreaMarker() {
        Point topLeft = _areaMarker.getLocation();
        Point bottomRight = new Point(topLeft.x + (int) _areaMarker.getWidth(), topLeft.y + (int) _areaMarker.getHeight());
        MapSquare topLeftSquare = getSquare(topLeft);
        MapSquare rightBottomSquare = getSquare(bottomRight);

        if (topLeftSquare != null && rightBottomSquare != null) {
            renderSquares(getSquare(topLeft).getLocation(), getSquare(bottomRight).getLocation(), false);
            addDirtyRect(_areaMarker);
        }
    }

    /**
     *
     */
    protected void renderAreaMarker() {
        Graphics2D graphics = getBufferGraphics();

        graphics.setPaint(Color.white);
        graphics.setStroke(new BasicStroke(1));
        graphics.draw(_areaMarker);
        addDirtyRect(_areaMarker);
    }

    /**
     *
     */
    protected Point convertMainCanvasPoint(Point point) {
        point = new Point(point);
        point.x /= MapSquare.WIDTH;
        point.y /= MapSquare.HEIGHT;
        point.x *= MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH;
        point.y *= MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT;

        return point;
    }

    /**
     *
     */
    protected Dimension convertMainCanvasDimension(Dimension size) {
        size = new Dimension(size);
        size.width /= MapSquare.WIDTH;
        size.height /= MapSquare.HEIGHT;
        size.width *= MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH;
        size.height *= MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT;

        return size;
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
    protected void paintBackground(Graphics2D graphics, Point topLeft, Point bottomRight) {
        Rectangle rect = new Rectangle(topLeft.x * MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH,
                topLeft.y * MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT,
                (bottomRight.x - topLeft.x) * MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH,
                (bottomRight.y - topLeft.y) * MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT);
        graphics.setPaint(Color.blue);
        graphics.fill(rect);
        graphics.draw(rect);
    }

    /**
     *
     */
    protected void scrollMapTo(Point point) {
        MapSquare square = getSquare(point);

        if (square != null) {
            getController().scrollTo(getSquare(point).getLocation());
        }
    }

    /**
     *
     */
    protected MapSquare getSquare(Point point) {
        return getController().getMap().getSquareAt(new Point(point.x / MapSquareRenderer.THUMBNAIL_SQUARE_WIDTH,
                point.y / MapSquareRenderer.THUMBNAIL_SQUARE_HEIGHT));
    }

    /**
     *
     */
    protected class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2 && MainWindow.getInstance().isGameInProgress()) {
                scrollMapTo(event.getPoint());
            }
        }
    }
}
