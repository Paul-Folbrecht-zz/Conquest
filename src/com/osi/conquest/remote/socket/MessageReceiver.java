package com.osi.conquest.remote.socket;


import com.osi.conquest.ConquestException;
import com.osi.conquest.Logger;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.remote.MessageIDs;

import java.net.Socket;
import java.util.HashMap;


/**
 * This Runnable object reads a single message from the a socket.  It blocks until the message
 * is received.
 *
 * @author Paul Folbrecht
 */
public class MessageReceiver implements Runnable {
    protected HashMap _handlers;
    protected Socket _socket;
    protected boolean _run = true;

    /**
     *
     */
    public MessageReceiver(Socket socket, HashMap handlers) {
        _handlers = handlers;
        _socket = socket;
    }

    /**
     *
     */
    public Socket getSocket() {
        return _socket;
    }

    /**
     *
     */
    public void closeConnection() throws Exception {
        stopListening();
        synchronized (_socket) {
            _socket.close();
        }
    }

    /**
     *
     */
    public void stopListening() {
        _run = false;
    }

    /**
     *
     */
    public void beforeAck(Object[] message) {
        int id = ((Integer) message[0]).intValue();

        if (MessageIDs.isLastMessage(id)) {
            stopListening();
        }
    }

    /**
     *
     */
    public boolean isRunning() {
        return _run;
    }

    /**
     *
     */
    public void run() {
        try {
            while (_run) {
                Logger.info("Waiting for incoming message..");
                handleMessage(this, SocketUtils.receiveMessage(this, _socket));
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    /**
     *
     */
    protected void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
        if (message != null) {
            Integer id = (Integer) message[0];
            MessageHandler handler = (MessageHandler) _handlers.get(id);

            if (handler == null) {
                throw new ConquestException("No handler for message id " + id);
            }

            handler.handleMessage(this, message);
        }
    }

    /**
     *
     */
    protected void handleError(Exception e) {
        if (ConquestFactory.getController() != null) {
            ConquestFactory.getController().showError("Communications error", e);
        }
    }

    /**
     *
     */
    public static interface MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception;
    }
}
