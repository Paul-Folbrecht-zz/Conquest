package com.osi.conquest.remote.host;


import com.osi.conquest.ConquestException;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.Player;
import com.osi.conquest.domain.impl.HostGameController;
import com.osi.conquest.remote.PlayerConnectListener;


/**
 * This is the interface to the "Host" object.  The Host is responsible for
 * managing remote games.  It handles all sending of messages to clients.
 *
 * @author Paul Folbrecht
 */
public abstract class Host {
    protected static Host _instance;

    public static Host getInstance() {
        if (_instance == null) {
            _instance = new com.osi.conquest.remote.host.socket.SocketHost();
        }

        return _instance;
    }

    public abstract int getListeningPort();

    public abstract void waitForPlayers(PlayerConnectListener listener, Player[] players,
                                        boolean isNewGame) throws ConquestException;

    public abstract Thread initClients(Player[] players) throws ConquestException;

    public abstract void activateRemotePlayer(Player player) throws ConquestException;

    public abstract void sendPlayerMessage(String from, Player to, String msg)
            throws ConquestException;

    public abstract void sendHostQuittingMessage() throws ConquestException;

    public abstract void sendYouEliminatedMessage(Player player) throws ConquestException;

    public abstract void terminateReceivers() throws ConquestException;

    public abstract void terminateReceiver(Player player) throws ConquestException;

    /**
     *
     */
    protected HostGameController getController() {
        return (HostGameController) ConquestFactory.getController();
    }

    /**
     *
     */
    protected class NextPlayerWorker implements Runnable {
        protected Player[] _players;

        public NextPlayerWorker(Player[] players) {
            _players = players;
        }

        public void run() {
            getController().receiveGameState(0, _players);
        }
    }

    /**
     *
     */
    protected class PlayerMessageWorker implements Runnable {
        protected String _from;
        protected Player _to;
        protected String _msg;

        public PlayerMessageWorker(String from, Player to, String msg) {
            _from = from;
            _to = to;
            _msg = msg;
        }

        public void run() {
            getController().sendPlayerMessage(_from, _to, _msg);
        }
    }

    /**
     *
     */
    protected class PlayerQuittingWorker implements Runnable {
        protected Player _player;

        public PlayerQuittingWorker(Player player) {
            _player = player;
        }

        public void run() {
            getController().removePlayer(_player);
        }
    }

    /**
     *
     */
    protected class PlayerEliminatedWorker implements Runnable {
        protected Player _player;

        public PlayerEliminatedWorker(Player player) {
            _player = player;
        }

        public void run() {
            getController().playerEliminated(_player);
        }
    }
}
