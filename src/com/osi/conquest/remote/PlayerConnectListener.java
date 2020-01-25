package com.osi.conquest.remote;


import com.osi.conquest.domain.Player;


/**
 * @author Paul Folbrecht
 */
public interface PlayerConnectListener {
    public void playerConnected(Player player, Player[] allPlayers, boolean done);
}
