package com.osi.util;


import java.io.IOException;
import java.net.Socket;


/**
 * Utility class to safely open a socket.  The Java Socket API has a big
 * problem in that you can't set a timeout value until after construction
 * but the contructor itself initiates a connection and can hang indefinitely.
 * This class spawn a new thread to create the socket.
 *
 * @author Paul Folbrecht
 */
public class SocketOpener implements Runnable {
    protected String _host;
    protected int _port;
    protected Socket _socket;

    /**
     *
     */
    public static Socket openSocket(String host, int port, int timeout) {
        SocketOpener opener = new SocketOpener(host, port);
        Thread thread = new Thread(opener);

        thread.start();
        try {
            thread.join(timeout);
        } catch (InterruptedException e) {
            // Eat it.
        }

        return opener.getSocket();
    }

    /**
     *
     */
    public void run() {
        try {
            _socket = new Socket(_host, _port);
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException("Cannot open socket: " + e);
        }
    }

    /**
     *
     */
    protected SocketOpener(String host, int port) {
        _host = host;
        _port = port;
    }

    /**
     *
     */
    protected Socket getSocket() {
        return _socket;
    }
}
