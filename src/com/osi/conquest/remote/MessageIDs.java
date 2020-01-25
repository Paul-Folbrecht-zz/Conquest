package com.osi.conquest.remote;


/**
 * @author Paul Folbrecht
 */
public class MessageIDs {
    // Client to host messages.
    public static final int NEXT_PLAYER = 0;
    public static final int PLAYER_ELIMINATED = 1;
    public static final int PLAYER_QUITTING = 2;
    public static final int HOST_PLAYER_MESSAGE = 3;
    public static final int NEED_MAP = 4;

    // Host to client messages.
    public static final int INIT_GAME = 100;
    public static final int SEND_MAP = 101;
    public static final int ACTIVATE_PLAYER = 102;
    public static final int CLIENT_PLAYER_MESSAGE = 103;
    public static final int HOST_QUITTING = 104;
    public static final int YOU_ELIMINATED = 105;

    public static boolean isLastMessage(int id) {
        return (id == PLAYER_QUITTING || id == HOST_QUITTING || id == YOU_ELIMINATED);
    }
}
