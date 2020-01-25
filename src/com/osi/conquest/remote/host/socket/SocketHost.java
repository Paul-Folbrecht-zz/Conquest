package com.osi.conquest.remote.host.socket;


import com.osi.conquest.ConquestException;
import com.osi.conquest.ConquestRuntimeException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.Player;
import com.osi.conquest.remote.MessageIDs;
import com.osi.conquest.remote.PlayerConnectListener;
import com.osi.conquest.remote.host.Host;
import com.osi.conquest.remote.socket.MessageReceiver;
import com.osi.conquest.remote.socket.SocketUtils;
import com.osi.conquest.ui.MainWindow;

import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


/**
 * @author Paul Folbrecht
 */
public class SocketHost extends Host {
    /**
     * The port we listen for incoming requests on.
     */
    protected int _listenerPort;

    /**
     * Map of Players to MessageReceivers.  The socket is a connection to the machine that Player
     * belongs to.
     */
    protected HashMap _receivers = new HashMap();

    /**
     *
     */
    protected HashMap _handlers = new HashMap();

    /**
     *
     */
    public SocketHost() throws ConquestRuntimeException {
        _listenerPort = PropertyManager.getIntProperty("listener.port");

        _handlers.put(new Integer(MessageIDs.NEXT_PLAYER), new NextPlayerHandler());
        _handlers.put(new Integer(MessageIDs.HOST_PLAYER_MESSAGE), new PlayerMessageHandler());
        _handlers.put(new Integer(MessageIDs.PLAYER_QUITTING), new PlayerQuittingHandler());
        _handlers.put(new Integer(MessageIDs.PLAYER_ELIMINATED), new PlayerEliminatedHandler());
        _handlers.put(new Integer(MessageIDs.NEED_MAP), new SendMapHandler());
    }

    /**
     * @return The port we listen for incoming requests on.
     */
    public int getListeningPort() {
        return _listenerPort;
    }

    /**
     *
     */
    public void waitForPlayers(PlayerConnectListener listener, Player[] players, boolean isNewGame)
            throws ConquestException {
        // Start listener thread that will run till all players are connected.
        Logger.info("Waiting for remote players to connect..");
        new Thread(new ConnectListener(listener, players, isNewGame)).start();
    }

    /**
     *
     */
    public void terminateReceivers() throws ConquestException {
        Player[] players = getController().getPlayers();

        for (int index = 0; index < players.length; index++) {
            if (players[index].isLocalToHost() == false) {
                terminateReceiver(players[index]);
            }
        }
    }

    /**
     *
     */
    public void terminateReceiver(Player player) throws ConquestException {
        try {
            Logger.info("Terminating receiver for player " + player);
            getReceiver(player).closeConnection();
            _receivers.remove(new Integer(player.getId()));
        } catch (Exception e) {
            throw new ConquestException(e);
        }
    }

    /**
     *
     */
    public void activateRemotePlayer(Player player) throws ConquestException {
        SocketUtils.sendMessage(getSocket(player),
                new Object[]{new Integer(MessageIDs.ACTIVATE_PLAYER),
                        new Integer(getController().getCurrentPlayer().getId()), getController().getPlayers()});
    }

    /**
     *
     */
    public void sendPlayerMessage(String from, Player to, String msg) throws ConquestException {
        SocketUtils.sendMessage(getSocket(to),
                new Object[]{new Integer(MessageIDs.CLIENT_PLAYER_MESSAGE), from, msg});
    }

    /**
     *
     */
    public void sendHostQuittingMessage() throws ConquestException {
        Player[] players = getController().getPlayers();

        for (int index = 0; index < players.length; index++) {
            if (!players[index].isLocalToHost()) {
                getReceiver(players[index]).stopListening();
            }
        }
        sendMessageToAllClients(new Object[]{new Integer(MessageIDs.HOST_QUITTING)});
    }

    /**
     *
     */
    public void sendYouEliminatedMessage(Player player) throws ConquestException {
        getReceiver(player).stopListening();
        SocketUtils.sendMessage(getSocket(player),
                new Object[]{new Integer(MessageIDs.YOU_ELIMINATED)});
    }

    /**
     *
     */
    public Thread initClients(Player[] players) throws ConquestException {
        return new Thread(new InitClientsWorker(players));
    }

    /**
     *
     */
    protected void sendMessageToAllClients(Object[] message) throws ConquestException {
        Player[] players = getController().getPlayers();

        for (int index = 0; index < players.length; index++) {
            if (players[index].isLocalToHost() == false) {
                SocketUtils.sendMessage(getSocket(players[index]), message);
            }
        }
    }

    /**
     *
     */
    protected Socket getSocket(Player player) throws ConquestException {
        return getReceiver(player).getSocket();
    }

    /**
     *
     */
    protected MessageReceiver getReceiver(Player player) throws ConquestException {
        MessageReceiver receiver = (MessageReceiver) _receivers.get(new Integer(player.getId()));

        if (receiver == null) {
            throw new ConquestException("No receiver for player " + player);
        }

        return receiver;
    }

    /**
     *
     */
    protected class InitClientsWorker implements Runnable {
        protected Player[] _players;

        public InitClientsWorker(Player[] players) {
            _players = players;
        }

        public void run() {
            try {
                for (int index = 0; index < _players.length; index++) {
                    if (_players[index].isLocalToHost() == false) {
                        SocketUtils.sendMessage(getSocket(_players[index]), new Object[]
                                {new Integer(MessageIDs.INIT_GAME), getController().getMapFilename(), _players,
                                        new Integer(_players[index].getId())});
                    }
                }
            } catch (ConquestException e) {
                MainWindow.getInstance().showError("Communications error", e);
            }
        }
    }

    /**
     *
     */
    protected abstract class Listener implements Runnable {
        protected boolean _done = false;

        public void run() {
            try {
                ServerSocket listener = new ServerSocket(_listenerPort);

                while (!_done) {
                    Socket socket = listener.accept();

                    Logger.info("Got connection from " + socket.getInetAddress().getHostAddress());
                    handleConnection(socket);
                }
            } catch (Exception e) {
                getController().showError("Communications error", e);
            }
        }

        protected abstract void handleConnection(Socket socket) throws Exception;
    }

    /**
     *
     */
    protected class ConnectListener extends Listener {
        protected PlayerConnectListener _listener;
        protected Player[] _players;
        protected boolean _isNewGame;
        protected int _playersToWaitFor;

        public ConnectListener(PlayerConnectListener listener, Player[] players, boolean isNewGame) {
            _listener = listener;
            _players = players;
            _isNewGame = isNewGame;

            for (int index = 0; index < players.length; index++) {
                if (players[index] == null || !players[index].isLocalToHost()) {
                    _playersToWaitFor++;
                }
            }
        }

        protected void handleConnection(Socket socket) throws Exception {
            Object[] message = SocketUtils.receiveMessage(null, socket);
            Player player;

            player = getPlayer((String) message[0]);
            if (player != null) {
                SocketUtils.sendMessage(socket, new Object[]{"ok"});
                startReceiver(socket, player);

                // Check if we're done.
                if (_receivers.size() == _playersToWaitFor) {
                    _done = true;
                }
                player.connected();
                _listener.playerConnected(player, _players, _done);
            } else {
                SocketUtils.sendMessage(socket, new Object[]{"rejected"});
            }
        }

        private void startReceiver(Socket socket, Player player) {
            Integer id = new Integer(player.getId());
            _receivers.put(id, new MessageReceiver(socket, _handlers));
            new Thread((MessageReceiver) _receivers.get(id)).start();
        }

        private Player getPlayer(String name) {
            for (int index = 0; index < _players.length; index++) {
                if (_isNewGame) {
                    // Look for the first empty slot in the array.
                    if (_players[index] == null) {
                        _players[index] = ConquestFactory.createPlayer(index, name, false);
                        return _players[index];
                    }
                } else {
                    // If game is being restored, search for a player with the passed name.
                    if (_players[index].getName().equalsIgnoreCase(name) &&
                            _players[index].isLocalToHost() == false) {
                        return _players[index];
                    }
                }
            }

            return null;
        }
    }

    /**
     *
     */
    protected class NextPlayerHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            SwingUtilities.invokeLater(new NextPlayerWorker((Player[]) message[1]));
        }
    }

    /**
     *
     */
    protected class PlayerMessageHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            Player to = getController().getPlayerWithId(((Integer) message[2]).intValue());
            SwingUtilities.invokeLater(new PlayerMessageWorker((String) message[1], to, (String) message[3]));
        }
    }

    /**
     *
     */
    protected class PlayerQuittingHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            Player player = getController().getPlayerWithId(((Integer) message[1]).intValue());
            SwingUtilities.invokeLater(new PlayerQuittingWorker(player));
        }
    }

    /**
     *
     */
    protected class PlayerEliminatedHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            Player player = getController().getPlayerWithId(((Integer) message[1]).intValue());
            SwingUtilities.invokeLater(new PlayerEliminatedWorker(player));
        }
    }

    /**
     *
     */
    protected class SendMapHandler implements MessageReceiver.MessageHandler {
        public void handleMessage(MessageReceiver receiver, Object[] message) throws Exception {
            Player player = getController().getPlayerWithId(((Integer) message[1]).intValue());
            SocketUtils.sendMessage(getSocket(player), new Object[]
                    {new Integer(MessageIDs.SEND_MAP),
                            getController().getMap(), getController().getPlayers(), new Integer(player.getId())});
        }
    }
}
