package com.osi.conquest.remote.socket;


import com.osi.conquest.ConquestException;
import com.osi.conquest.Logger;
import com.osi.conquest.ui.MainWindow;
import com.osi.util.StringUtils;

import java.io.*;
import java.net.Socket;


/**
 * All-static class of socket utility methods.
 *
 * @author Paul Folbrecht
 * @version 1.0
 */

public class SocketUtils {
    public static final int READ_INTERVAL = 500;

    /**
     *
     */
    public static void sendMessage(Socket socket, Object[] message) throws ConquestException {
        try {
            Logger.info("Obtaining socket lock for sending.");
            synchronized (socket) {
                ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                Logger.info("Sending socket message to " + socket.getInetAddress() + ": " +
                        StringUtils.toString(message));
                socket.setSoTimeout(0);
                stream.writeObject(new Integer(message.length));
                for (int index = 0; index < message.length; index++) {
                    stream.writeObject(message[index]);
                }
                stream.flush();
                listenForAck(socket);
            }
        } catch (Exception e) {
            throw new ConquestException("Communications error", e);
        }
    }

    /**
     *
     */
    public static Object[] receiveMessage(MessageReceiver receiver, Socket socket)
            throws ConquestException {
        try {
            while (receiver == null || receiver.isRunning()) {
                try {
                    synchronized (socket) {
                        socket.setSoTimeout(READ_INTERVAL);
                        ObjectInputStream stream =
                                new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                        int count = ((Integer) stream.readObject()).intValue();
                        Object[] message = new Object[count];

                        socket.setSoTimeout(0);
                        for (int index = 0; index < count; index++) {
                            message[index] = stream.readObject();
                        }
                        if (receiver != null) {
                            // Give the receiver a chance to stop the loop.  Must do this before the ack is sent
                            // because if this message says the connection is closing, this receiver should not
                            // attempt another message read.
                            receiver.beforeAck(message);
                        }
                        sendAck(socket);
                        Logger.info("Received socket message from " + socket.getInetAddress() + ": " +
                                StringUtils.toString(message));

                        return message;
                    }
                } catch (InterruptedIOException e) {
                    // Give anybody else that's trying to access this socket (for sending) a chance.
                    Thread.currentThread().yield();
                }
            }
        } catch (Exception e) {
            Logger.error(e);
            throw new ConquestException("Communications error", e);
        }

        return null;
    }

    /**
     *
     */
    public static void sendAck(Socket socket) throws Exception {
        ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        stream.writeObject("ack");
        stream.flush();
        Logger.info("Sent ack.");
    }

    /**
     *
     */
    public static void listenForAck(Socket socket) throws Exception {
        ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        Object message = stream.readObject();

        if (message instanceof String && message.equals("ack")) {
            Logger.info("Ack received.");
        } else {
            Logger.warn("Waiting for ack and we got something else: " + message);
        }
    }

    /**
     *
     */
    public static class SendMessageWorker implements Runnable {
        protected Socket _socket;
        protected Object[] _message;

        public SendMessageWorker(Socket socket, Object[] message) {
            _socket = socket;
            _message = message;
        }

        public void run() {
            try {
                ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(_socket.getOutputStream()));

                Logger.info("Sending socket message: " + StringUtils.toString(_message));
                stream.writeObject(new Integer(_message.length));
                for (int index = 0; index < _message.length; index++) {
                    stream.writeObject(_message[index]);
                }
                stream.flush();
                listenForAck(_socket);
            } catch (Exception e) {
                MainWindow.getInstance().showError("Communications error", e);
            }
        }
    }
}
