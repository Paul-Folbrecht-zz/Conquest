package com.osi.util;


/**
 * @author Paul Folbrecht
 */

public class StringUtils {
    /**
     *
     */
    public static String stripClassName(Class theClass) {
        return theClass.getName().substring(theClass.getName().
                lastIndexOf(".") + 1);
    }

    /**
     *
     */
    public static String toString(Object[] array) {
        StringBuffer buffer = new StringBuffer(500);

        for (int index = 0; index < array.length; index++) {
            buffer.append(array[index].toString());
            if (index != array.length - 1) {
                buffer.append(", ");
            }
        }

        return buffer.toString();
    }
}
