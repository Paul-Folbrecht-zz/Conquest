package com.osi.util.ui;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * This abstract class provides the basic functionality needed for a double-buffered drawing surface.
 *
 * @author Paul Folbrecht
 */

public abstract class DoubleBufferedCanvas extends JComponent {
    protected BufferedImage _buffer;
    protected Rectangle _dirtyRect = new Rectangle(0, 0);
    protected boolean _blankScreen = true;

    public DoubleBufferedCanvas() {
        setBorder(BorderFactory.createRaisedBevelBorder());
    }

    /**
     *
     */
    public void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;

        if (_buffer == null || _blankScreen) {
            graphics.setPaint(Color.darkGray);
            graphics.draw(new Rectangle(new Point(0, 0), getSize()));
        } else {
            graphics.drawImage(_buffer, 0, 0, this);
        }
    }

    /**
     *
     */
    public void addDirtyRect(Rectangle rect) {
        rect = new Rectangle(rect);
        rect.grow(1, 1);
        if (_dirtyRect.isEmpty()) {
            _dirtyRect = rect;
        } else {
            _dirtyRect = _dirtyRect.union(rect);
        }
    }

    /**
     *
     */
    public Graphics2D getBufferGraphics() {
        return _buffer.createGraphics();
    }

    /**
     * Blits the dirty region from the offscreen buffer to the display.
     */
    public void draw() {
        Graphics2D graphics = (Graphics2D) getGraphics();

        graphics.setClip(_dirtyRect);
        paintComponent(graphics);
        _dirtyRect = new Rectangle(0, 0);
    }

    /**
     *
     */
    public void setBlankScreen() {
        _blankScreen = true;
        repaint();
    }

    /**
     *
     */
    public void unsetBlankScreen() {
        _blankScreen = false;
        renderAndDrawAll();
    }

    /**
     *
     */
    public abstract void renderAndDrawAll();

    /**
     *
     */
    protected void init(Dimension size) {
        setSize(size);
        setPreferredSize(size);
        _buffer = null;
        // Run the GC to free as much memory as possible- I've seen OutOfMemory errors trying to
        // create very large images.
        System.gc();
        _buffer = getGraphicsConfiguration().createCompatibleImage((int) size.getWidth(), (int) size.getHeight());
        validate();
    }
}
