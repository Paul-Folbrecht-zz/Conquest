package com.osi.util;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


/**
 * @author Paul Folbrecht
 */
public class Main {
    private static int lines = 0;

    /**
     *
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: com.osi.util.Main <directory>.");
            System.exit(0);
        }

        countLines(new File(args[0]));
        System.out.println("Total lines in .java files: " + lines);
    }

    /**
     *
     */
    protected static void countLines(File file) {
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int index = 0; index < files.length; index++) {
                    countLines(files[index]);
                }
            } else if (file.getName().endsWith(".java")) {
                BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
                int data = stream.read();

                System.out.println("Processing " + file.getName());
                while (data != -1) {
                    if (data == '\n') {
                        lines++;
                    }
                    data = stream.read();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
