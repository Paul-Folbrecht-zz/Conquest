package com.osi.conquest.ui.event;


/**
 * Should be implemented by objects that want to know about game events.
 *
 * @author Paul Folbrecht
 */
public interface GameListener {
    public void onGameStarted();

    public void onGameEnded();

    public void onPlayerDeactivated();

    public void onPlayerActivated();
}
