package com.osi.conquest.remote.client;


import com.osi.conquest.ConquestException;
import com.osi.conquest.Logger;
import com.osi.conquest.PropertyManager;
import com.osi.conquest.domain.ConquestFactory;
import com.osi.conquest.domain.ConquestMap;
import com.osi.conquest.domain.GameController;
import com.osi.conquest.domain.Player;
import com.osi.conquest.ui.MainWindow;
import com.osi.util.Endpoint;


/**
 * @author Paul Folbrecht
 */
public abstract class ClientRemoteCommunicator {
    protected static ClientRemoteCommunicator[] _instances;

    /**
     *
     */
    static {
        try {
            _instances = new ClientRemoteCommunicator[1];
            _instances[0] = new com.osi.conquest.remote.client.socket.SocketClientRemoteCommunicator();
        } catch (ConquestException e) {
            Logger.error(e);
        }
    }

    /**
     *
     */
    public static ClientRemoteCommunicator getInstance() throws ConquestException {
        String type = PropertyManager.getProperty("communications.type");
        ClientRemoteCommunicator communicator = null;

        if (type == null || type.equalsIgnoreCase("socket")) {
            communicator = _instances[0];
        }

        return communicator;
    }

    /**
     *
     */
    public abstract void terminateReceiver() throws ConquestException;

    /**
     *
     */
    public abstract boolean connectToHost(Endpoint endpoint, String playerName)
            throws ConquestException;

    /**
     *
     */
    public abstract void sendNextPlayerMessage(Player[] players) throws ConquestException;

    /**
     *
     */
    public abstract void sendPlayerMessage(Player to, String msg) throws ConquestException;

    /**
     *
     */
    public abstract void sendPlayerQuittingMessage(Player player) throws ConquestException;

    /**
     *
     */
    public abstract void sendPlayerEliminatedMessage(Player player) throws ConquestException;

    /**
     *
     */
    protected static GameController getController() {
        return ConquestFactory.getController();
    }

    /**
     * This Worker should be activated to init a game started on a remote
     * machine.  It should be used for both new and restored games.
     */
    protected static class InitGameWorker implements Runnable {
        protected ConquestMap _map;
        protected Player[] _players;
        protected int _playerId;

        public InitGameWorker(ConquestMap map, Player[] players, int playerId) {
            _map = map;
            _players = players;
            _playerId = playerId;
        }

        public void run() {
            MainWindow.getInstance().receiveRemoteGame(_map, _players);
            getController().receiveGameState(_playerId, _players);
        }
    }

    /**
     * This Worker transfers the game state from a remote to the local machine,
     * activating the specified player in the process.
     */
    protected static class ActivatePlayerWorker implements Runnable {
        protected int _index;
        protected Player[] _players;

        public ActivatePlayerWorker(int index, Player[] players) {
            _index = index;
            _players = players;
        }

        public void run() {
            getController().receiveGameState(_index, _players);
            getController().activateLocalPlayer();
            MainWindow.getInstance().setState(MainWindow.STATE_NORMAL);
        }
    }

    /**
     * This worker displays a message sent from another player or the host.
     */
    protected static class PlayerMessageWorker implements Runnable {
        protected String _from;
        protected String _msg;

        public PlayerMessageWorker(String from, String msg) {
            _from = from;
            _msg = msg;
        }

        public void run() {
            MainWindow.getInstance().displayPlayerMessage(_from, _msg);
        }
    }

    /**
     *
     */
    protected static class HostQuittingWorker implements Runnable {
        public HostQuittingWorker() {
        }

        public void run() {
            MainWindow.getInstance().displayPlayerMessage("Host", "The Host has ended the game.");
            MainWindow.getInstance().endCurrentGame(false);
        }
    }

    /**
     *
     */
    protected static class LocalPlayerEliminatedWorker implements Runnable {
        public LocalPlayerEliminatedWorker() {
        }

        public void run() {
            MainWindow.getInstance().endCurrentGame(false);
        }
    }
}
