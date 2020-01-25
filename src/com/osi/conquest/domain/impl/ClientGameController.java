package com.osi.conquest.domain.impl;


import com.osi.conquest.ConquestException;
import com.osi.conquest.domain.Player;
import com.osi.conquest.remote.client.ClientRemoteCommunicator;
import com.osi.conquest.ui.MainWindow;


/**
 * @author Paul Folbrecht
 */
public class ClientGameController extends GameControllerImpl {
    /**
     *
     */
    public boolean isHost() {
        return false;
    }

    /**
     *
     */
    public void localPlayerQuitting() {
        try {
            ClientRemoteCommunicator.getInstance().sendPlayerQuittingMessage(getCurrentPlayer());
        } catch (ConquestException e) {
            showError("Cannot communicate with host", e);
        }
    }

    /**
     *
     */
    public void playerEliminated(Player player) {
        try {
            // If the local player was the one eliminated, the host will send a YOU_ELIMINATED
            // message which will end the game.
            ClientRemoteCommunicator.getInstance().sendPlayerEliminatedMessage(player);
            if (player != getCurrentPlayer()) {
                // We only need to remove the player if it wasn't our local player.  If it was, the game
                // will be terminated here by the host as a result of the "playerEliminated" message.  And,
                // in that case, we *don't* want to remove the player because then the current player will
                // be undefined during the brief window until the host terminates us.
                removePlayer(player);
            }
        } catch (ConquestException e) {
            showError("Cannot communicate with host", e);
        }
    }

    /**
     *
     */
/*  public void removePlayer( Player player ) {
    Player currentPlayer = getCurrentPlayer();

    super.removePlayer( player );
    if ( player == currentPlayer ) {
      activateNextPlayer();
    }
  }
*/

    /**
     *
     */
    public void endGame() {
        try {
            ClientRemoteCommunicator.getInstance().terminateReceiver();
        } catch (ConquestException e) {
            showError("Cannot communicate with host", e);
        }
    }

    /**
     *
     */
    public void receiveGameState(int playerId, Player[] players) {
        _players = players;
        getMap().setCitiesAndUnits(players);
        _currentPlayerIndex = playerIdToArrayIndex(playerId);
    }

    /**
     *
     */
    public void sendPlayerMessage(String from, Player player, String msg) {
        try {
            ClientRemoteCommunicator.getInstance().sendPlayerMessage(player, msg);
        } catch (ConquestException e) {
            showError("Cannot communicate with host", e);
        }
    }

    /**
     *
     */
    protected void activateNextPlayer() {
        try {
            ClientRemoteCommunicator.getInstance().sendNextPlayerMessage(_players);
            MainWindow.getInstance().setState(MainWindow.STATE_WAITING_FOR_REMOTE_PLAYER);
        } catch (ConquestException e) {
            showError("Cannot communicate with host", e);
        }
    }
}
