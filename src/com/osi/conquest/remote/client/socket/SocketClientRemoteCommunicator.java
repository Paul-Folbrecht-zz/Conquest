package com.osi.conquest.remote.client.socket;


import com.osi.conquest.ConquestException;
import com.osi.conquest.Logger;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.ConquestMap;
import com.osi.conquest.domain.Player;
import com.osi.conquest.remote.MessageIDs;
import com.osi.conquest.remote.client.ClientRemoteCommunicator;
import com.osi.conquest.remote.socket.MessageReceiver;
import com.osi.conquest.remote.socket.SocketUtils;
import com.osi.conquest.ui.MainWindow;
import com.osi.util.Endpoint;
import com.osi.util.SocketOpener;

import javax.swing.*;
import java.net.Socket;
import java.util.HashMap;


/**
 * @author Paul Folbrecht
 */
public class SocketClientRemoteCommunicator extends ClientRemoteCommunicator {
    /**
     *
     */
    protected static final int TIMEOUT = 30000;

    /**
     *
     */
    protected Socket _hostConnection;

    /**
     *
     */
    protected MessageReceiver _hostReceiver;

    /**
     *
     */
    protected HashMap _handlers = new HashMap();

    /**
     * Ctor.
     */
    public SocketClientRemoteCommunicator() throws ConquestException {
        _handlers.put(new Integer(MessageIDs.INIT_GAME), new InitGameHandler());
        _handlers.put(new Integer(MessageIDs.SEND_MAP), new ReceiveMapHandler());
        _handlers.put(new Integer(MessageIDs.ACTIVATE_PLAYER), new ActivatePlayerHandler());
        _handlers.put(new Integer(MessageIDs.CLIENT_PLAYER_MESSAGE), new PlayerMessageHandler());
        _handlers.put(new Integer(MessageIDs.HOST_QUITTING), new HostQuittingHandler());
        _handlers.put(new Integer(MessageIDs.YOU_ELIMINATED), new LocalPlayerEliminatedHandler());
    }

    /**
     *
     */
    public void terminateReceiver() throws ConquestException {
        if (_hostConnection != null) {
            Logger.info("Terminating host receiver.");
            try {
                _hostReceiver.closeConnection();
                _hostReceiver = null;
                _hostConnection = null;
            } catch (Exception e) {
                throw new ConquestException("Could not close host connection", e);
            }
        }
    }

    /**
     *
     */
    public boolean connectToHost(Endpoint endpoint, String playerName) throws ConquestException {
        _hostConnection = SocketOpener.openSocket(endpoint.getAddress().getHostAddress(), endpoint.getPort(), TIMEOUT);

        if (_hostConnection == null) {
            throw new ConquestException("Could not connect to host.");
        }

        SocketUtils.sendMessage(_hostConnection, new Object[]{playerName});
        Object[] message = SocketUtils.receiveMessage(null, _hostConnection);

        if (message[0].equals("ok")) {
            startHostReceiver();
        }

        return (message[0].equals("ok"));
    }

    /**
     *
     */
    public void sendNextPlayerMessage(Player[] players) throws ConquestException {
        SocketUtils.sendMessage(_hostConnection,
                new Object[]{new Integer(MessageIDs.NEXT_PLAYER), players});
    }

    /**
     *
     */
    public void sendPlayerMessage(Player to, String msg) throws ConquestException {
        SocketUtils.sendMessage(_hostConnection, new Object[]
                {new Integer(MessageIDs.HOST_PLAYER_MESSAGE),
                        getController().getCurrentPlayer().toString(), new Integer(to.getId()), msg});
    }

    /**
     *
     */
    public void sendPlayerQuittingMessage(Player player) throws ConquestException {
        if (_hostConnection != null) {
            _hostReceiver.stopListening();
            SocketUtils.sendMessage(_hostConnection,
                    new Object[]{new Integer(MessageIDs.PLAYER_QUITTING), new Integer(player.getId())});
        }
    }

    /**
     *
     */
    public void sendPlayerEliminatedMessage(Player player) throws ConquestException {
        if (_hostConnection != null) {
            SocketUtils.sendMessage(_hostConnection, new Object[]
                    {new Integer(MessageIDs.PLAYER_ELIMINATED), new Integer(player.getId())});
        }
    }

    /**
     *
     */
    protected void startHostReceiver() throws ConquestException {
        _hostReceiver = new MessageReceiver(_hostConnection, _handlers);
        new Thread(_hostReceiver).start();
    }

    /**
     *
     */
    protected class InitGameHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            try {
                String mapFilename = (String) message[1];
                ConquestMap map = ConquestFactory.loadMap(mapFilename);

                if (map == null) {
                    Thread.currentThread().sleep(500);
                    // No map here.  Host must send it.  Init the game when it arrives, via another message.
                    SocketUtils.sendMessage(_hostConnection,
                            new Object[]{new Integer(MessageIDs.NEED_MAP), (Integer) message[3]});
                } else {
                    SwingUtilities.invokeLater(new ClientRemoteCommunicator.InitGameWorker(map, (Player[]) message[2], ((Integer) message[3]).intValue()));
                }
            } catch (ConquestException e) {
                MainWindow.getInstance().showError("Communications error", e);
            }
        }
    }

    /**
     *
     */
    protected class ReceiveMapHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            SwingUtilities.invokeLater(new ClientRemoteCommunicator.InitGameWorker((ConquestMap) message[1], (Player[]) message[2], ((Integer) message[3]).intValue()));
        }
    }

    /**
     *
     */
    protected class ActivatePlayerHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            SwingUtilities.invokeLater(new ClientRemoteCommunicator.ActivatePlayerWorker(((Integer) message[1]).intValue(), (Player[]) message[2]));
        }
    }

    /**
     *
     */
    protected class PlayerMessageHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            SwingUtilities.invokeLater(new ClientRemoteCommunicator.PlayerMessageWorker((String) message[1], (String) message[2]));
        }
    }

    /**
     *
     */
    protected class HostQuittingHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            SwingUtilities.invokeLater(new ClientRemoteCommunicator.HostQuittingWorker());
        }
    }

    /**
     *
     */
    protected class LocalPlayerEliminatedHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            SwingUtilities.invokeLater(new ClientRemoteCommunicator.LocalPlayerEliminatedWorker());
        }
    }
}
