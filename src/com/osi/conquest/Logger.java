package com.osi.conquest;


//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Category;
//import org.apache.log4j.PropertyConfigurator;


/**
 * @author Paul Folbrecht
 */
public class Logger {
//    protected static Category _logger = Category.getInstance("Conquest");

    /**
     * Init log4j.
     */
//    static {
//        try {
//            String configFile = (String) PropertyManager.getProperty("log4j.configfile");
//            PropertyConfigurator.configure(configFile);
//        } catch (ConquestRuntimeException e) {
//            // Use BasicConfigurator by default.
//            BasicConfigurator.configure();
//        }
//    }

    /**
     *
     */
    public static void debug(String text) {
        System.out.println(text);
//        _logger.debug(text);
    }

    /**
     *
     */
    public static void info(String text) {
        System.out.println(text);
//        _logger.info(text);
    }

    /**
     *
     */
    public static void warn(String text) {
        System.out.println(text);
//        _logger.warn(text);
    }

    /**
     *
     */
    public static void error(String text) {
        System.out.println(text);
//        _logger.error(text);
        new Throwable().printStackTrace();
    }

    /**
     *
     */
    public static void error(Exception e) {
        new Throwable().printStackTrace();
//        _logger.error(e);
        e.printStackTrace();
    }

    /**
     *
     */
    public static void error(String text, Exception e) {
        new Throwable().printStackTrace();
//        _logger.error(text, e);
        e.printStackTrace();
    }
}
