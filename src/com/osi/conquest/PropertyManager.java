package com.osi.conquest;


import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * @author Paul Folbrecht
 */
public class PropertyManager {
    protected static Properties _properties = new Properties();
    protected static Properties _preferences = new Properties();

    /**
     *
     */
    static {
        String separator = System.getProperty("file.separator");

        try {
            _properties.load(new FileInputStream(Main.getHomeDirectory() + separator + "conquest.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load property file: " + e);
        }

        try {
            _preferences.load(new FileInputStream(Main.getHomeDirectory() + separator + "conquestPrefs.properties"));
        } catch (IOException e) {
            // Create it.
            _preferences = new Properties();
            savePrefs();
        }
    }

    /**
     *
     */
    public static String getMapPath() {
        String separator = System.getProperty("file.separator");
        return Main.getHomeDirectory() + separator + "maps" + separator;
    }

    /**
     *
     */
    public static String getImagePath() {
        String separator = System.getProperty("file.separator");
        return Main.getHomeDirectory() + separator + "images" + separator;
    }

    /**
     *
     */
    public static String getProperty(String key) throws ConquestRuntimeException {
        return getProperty(_properties, key);
    }

    /**
     *
     */
    public static int getIntProperty(String key) throws ConquestRuntimeException {
        return getIntProperty(_properties, key);
    }

    /**
     *
     */
    public static String getPref(String key) throws ConquestRuntimeException {
        return getProperty(_preferences, key);
    }

    /**
     *
     */
    public static int getIntPref(String key) throws ConquestRuntimeException {
        return getIntProperty(_preferences, key);
    }

    /**
     *
     */
    public static void setPref(String key, String data) {
        _preferences.setProperty(key, data);
    }

    /**
     *
     */
    public static void savePrefs() {
        try {
            String separator = System.getProperty("file.separator");
            _preferences.store(new FileOutputStream(Main.getHomeDirectory() + separator + "conquestPrefs.properties"), null);
        } catch (IOException e) {
            Logger.error("Could not write preferences.", e);
        }
    }

    /**
     *
     */
    public static Image getImage(String name) throws ConquestException {
        MediaTracker tracker = new MediaTracker(new JLabel());
        Image image = Toolkit.getDefaultToolkit().getImage(getImagePath() + name + ".jpg");

        tracker.addImage(image, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
        }

        return image;
    }

    /**
     *
     */
    public static BufferedImage getBufferedImage(String name) throws ConquestException {
        try {
            JPEGImageDecoder decoder =
                    JPEGCodec.createJPEGDecoder(new FileInputStream(getImagePath() + name + ".jpg"));
            return decoder.decodeAsBufferedImage();
        } catch (Exception e) {
            throw new ConquestException(e.getMessage());
        }
    }

    /**
     *
     */
    protected static String getProperty(Properties props, String key)
            throws ConquestRuntimeException {
        String value = props.getProperty(key);

        if (value == null) {
            throw new ConquestRuntimeException("Missing property key: " + key);
        }

        return value;
    }

    /**
     *
     */
    protected static int getIntProperty(Properties props, String key)
            throws ConquestRuntimeException {
        try {
            return Integer.parseInt(getProperty(props, key));
        } catch (NumberFormatException e) {
            throw new ConquestRuntimeException("Invalid integer property: " + key, e);
        }
    }
}
