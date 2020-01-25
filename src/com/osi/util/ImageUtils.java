package com.osi.util;


import com.osi.conquest.ConquestException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;


/**
 * @author Paul Folbrecht
 */
public class ImageUtils {
    /**
     *
     */
    public static Image getTransparentImage(GraphicsConfiguration graphicsConfig,
                                            Image image, int width, int height) {
        return getTintedImage(graphicsConfig, image, width, height, null);
    }

    /**
     *
     */
    public static Image getTintedImage(GraphicsConfiguration graphicsConfig,
                                       Image image, int width, int height, Color tint) {
        int[] pixels = new int[width * height];
        PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);

        try {
            grabber.grabPixels();
        } catch (InterruptedException e) {
            Logger.error(e);
        }

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int pixel = pixels[j * width + i];
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (red <= 30 && green <= 30 && blue <= 30) {
                    // Make the image transparent- anything black (or close to it) has it's alpha set to 0.
                    pixels[j * width + i] = 0;
                } else if (tint != null) {
                    red   = averageComponent( red, tint.getRed() );
                    green = averageComponent( green, tint.getGreen() );
                    blue  = averageComponent( blue, tint.getBlue() );
                    pixels[j * width + i] = ( alpha << 24 ) | ( red << 16 ) | ( green << 8 ) | blue;
                }
            }
        }

        image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));

        return createBufferedImage(graphicsConfig, image, width, height);
    }

    /**
     *
     */
    public static BufferedImage convertToGrayScale(BufferedImage image) {
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(image, image);

        return image;
    }

    /**
     *
     */
    public static BufferedImage createBufferedImage(GraphicsConfiguration graphicsConfig, Image image, int width, int height) {
        BufferedImage buffer = graphicsConfig.createCompatibleImage(width, height, Transparency.BITMASK);
        Graphics2D graphics = (Graphics2D) buffer.getGraphics();

        graphics.drawImage(image, 0, 0, null);

        return buffer;
    }

    /**
     *
     */
    public static BufferedImage cloneBufferedImage(BufferedImage image) {
        return new BufferedImage(image.getColorModel(), image.copyData(null),
                image.isAlphaPremultiplied(), null);
    }

    /**
     *
     */
    public static Cursor createCursor(GraphicsConfiguration graphicsConfig,
                                      String imageName, Point hotSpot, Dimension size) throws ConquestException {
        return Toolkit.getDefaultToolkit().createCustomCursor(getTransparentImage(graphicsConfig,
                PropertyManager.getImage(imageName), size.width, size.height), hotSpot, imageName);
    }

    /**
     *
     */
    protected static int averageComponent(int one, int two) {
        int average = (one + two) / 2;

        if (average > 255) {
            average = 255;
        }

        return average;
    }
}
