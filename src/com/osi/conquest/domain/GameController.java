package com.osi.conquest.domain;


import com.osi.conquest.ConquestException;

import java.awt.*;
import java.io.Serializable;


/**
 * @author Paul Folbrecht
 */
public interface GameController extends Serializable, Cloneable {
    public ConquestMap getMap();

    public String getMapFilename();

    public Player[] getPlayers();

    public Player getCurrentPlayer();

    public Player getPlayerWithId(int id);

    public Unit getSelectedUnit();

    public boolean isHost();

    public boolean anyRemotePlayers(Player[] players);

    public void setMapFilename(String name);

    public void loadMap() throws ConquestException;

    public void waitCursorOn();

    public void waitCursorOff();

    public void displayPlayerMessage(String from, String msg);

    public void init(ConquestMap map, Player[] players);

    public void showError(String msg, Exception e);

    public void newGame();

    public void gameRestored();

    public void endGame();

    public void endOfTurn();

    public void activateLocalPlayer();

    public void localPlayerQuitting();

    public void removePlayer(Player player);

    public void receiveGameState(int playerIndex, Player[] players);

    public boolean nextUnit(Unit unitToSkip);

    public void scrollTo(Point location);

    public void setSelectedUnit(Unit unit);

    public void deselectUnit();

    public void setGameDirty();

    public void paintProductionModeSymbols(Graphics2D graphics);

    public boolean combat(Graphics2D graphics, Unit attacker, Point location);

    public void sendPlayerMessage(String from, Player player, String msg);

    public void showCityDialog(Player player, City city, boolean readOnly);

    public void onUnitOutOfGas(Unit unit);

    public void onUnitDestroyed(Unit unit);

    public void unitDefeated(Unit winner, Unit loser);

    public void modelChanged(MapSquare square);

    public void updateUI(Rectangle area);

    public void noUnitsToMove();

    public void unitMoved(Unit unit, Point start, Point end);

    public void unitFought(Unit unit, OwnableObject opponent, boolean won);

    public void checkForPlayerElimination(Player player);
}
