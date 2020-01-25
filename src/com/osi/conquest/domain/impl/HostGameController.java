package com.osi.conquest.domain.impl;


import com.osi.conquest.ConquestException;
import com.osi.conquest.domain.Player;
import com.osi.conquest.remote.host.Host;
import com.osi.conquest.ui.MainWindow;
import com.osi.util.ui.dialogs.DialogUtils;
import com.osi.util.ui.dialogs.PanelWrapperDialog;
import com.osi.util.ui.panels.ThreadProgressPanel;


/**
 * @author Paul Folbrecht
 */
public class HostGameController extends GameControllerImpl {
    protected Player _lastLocalPlayer;

    /**
     *
     */
    public boolean isHost() {
        return true;
    }

    /**
     *
     */
    public Player getCurrentPlayer() {
        if (_players != null && _currentPlayerIndex < _players.length) {
            if (MainWindow.getInstance().getState() == MainWindow.STATE_WAITING_FOR_REMOTE_PLAYER) {
                return _lastLocalPlayer;
            } else {
                return _players[_currentPlayerIndex];
            }
        }

        return null;
    }

    /**
     *
     */
    public void newGame() {
        super.newGame();
        initClients();
        _lastLocalPlayer = _players[0];
    }

    /**
     *
     */
    public void gameRestored() {
        initClients();
    }

    /**
     *
     */
    public void localPlayerQuitting() {
    }

    /**
     * Called when a player is eliminated on any machine- will be called by a message handler in
     * the case of remote machines.
     */
    public void playerEliminated(Player player) {
        sendPlayerMessage("Host", player, "You have been defeated!");

        if (!player.isLocalToHost()) {
            try {
                // Send a message to the player's machine that will result in the game being ended there.
                Host.getInstance().sendYouEliminatedMessage(player);
            } catch (ConquestException e) {
                showError("Communications error", e);
            }
        }

        removePlayer(player);
    }

    /**
     *
     */
    public void endGame() {
        try {
            Host.getInstance().sendHostQuittingMessage();
            Host.getInstance().terminateReceivers();
        } catch (ConquestException e) {
            showError("Communications error", e);
        }
    }

    /**
     *
     */
    public void removePlayer(Player player) {
        Player currentPlayer = getActualCurrentPlayer();

        super.removePlayer(player);

        if (!player.isLocalToHost()) {
            try {
                Host.getInstance().terminateReceiver(player);
            } catch (ConquestException e) {
                showError("Communications error", e);
            }
        }

        if (_players.length > 0) {
            sendPlayerMessageToAll(player.toString() + " has been defeated!");

            if (player == currentPlayer && _players.length > 0) {
                activateNextPlayer();
            }
        }
    }

    /**
     *
     */
    public void sendPlayerMessage(String from, Player player, String msg) {
        if (player.isLocalToHost()) {
            if (getCurrentPlayer() == player) {
                MainWindow.getInstance().displayPlayerMessage(from, msg);
            } else {
                player.addMessage(from, msg);
            }
        } else {
            try {
                Host.getInstance().sendPlayerMessage(from, player, msg);
            } catch (ConquestException e) {
                showError("Cannot communicate with " + player, e);
            }
        }
    }

    /**
     *
     */
    public void sendPlayerMessageToAll(String msg) {
        for (int index = 0; index < _players.length; index++) {
            sendPlayerMessage("Host", _players[index], msg);
        }
    }

    /**
     *
     */
    public void receiveGameState(int playerIndex, Player[] players) {
        _players = players;
        getMap().setCitiesAndUnits(players);
        activateNextPlayer();
    }

    /**
     *
     */
    protected void activateNextPlayer() {
        nextPlayer();
        MainWindow.getInstance().setState(MainWindow.STATE_NORMAL);

        if (getActualCurrentPlayer().isLocalToHost()) {
            activateLocalPlayer();
        } else {
            try {
                Host.getInstance().activateRemotePlayer(getActualCurrentPlayer());
                MainWindow.getInstance().setState(MainWindow.STATE_WAITING_FOR_REMOTE_PLAYER);
            } catch (ConquestException e) {
                showError("Cannot communicate with " + getActualCurrentPlayer(), e);
            }
        }
    }

    /**
     *
     */
    protected void initClients() {
        if (anyRemotePlayers(getPlayers())) {
            PanelWrapperDialog dlg = null;

            try {
                Thread thread = Host.getInstance().initClients(getPlayers());
                ThreadProgressPanel panel =
                        new ThreadProgressPanel("Initializing remote players...", thread);

                dlg = new PanelWrapperDialog(MainWindow.getInstance(), "Conquest", true,
                        panel, new String[0]);
                DialogUtils.showCentered(MainWindow.getInstance(), dlg);
            } catch (ConquestException e) {
                dlg.setVisible(false);
                showError("Communications error", e);
            }
        }
    }

    /**
     *
     */
    protected Player getActualCurrentPlayer() {
        if (_players != null && _currentPlayerIndex < _players.length) {
            return _players[_currentPlayerIndex];
        }

        return null;
    }

    /**
     * This method advances the game state to the next player.
     */
    protected void nextPlayer() {
        if (getActualCurrentPlayer().isLocalToHost()) {
            _lastLocalPlayer = getActualCurrentPlayer();
        }
        super.nextPlayer();
    }
}
