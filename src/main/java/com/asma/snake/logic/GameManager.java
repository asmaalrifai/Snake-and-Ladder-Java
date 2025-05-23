package com.asma.snake.logic;

import com.asma.snake.model.Board;
import com.asma.snake.model.Dice;
import com.asma.snake.model.Player;

/**
 * Handles the core logic of the Snake and Ladder game.
 * Manages player turns, dice rolls, movement, and win conditions.
 */
public class GameManager {

    private final Player player1;
    private final Player player2;
    private final Dice dice;
    private final Board board;

    private Player currentPlayer;
    private int lastRawPosition = -1;
    private int lastFinalPosition = -1;

    /**
     * Initializes the game with two players and a new board and dice.
     */
    public GameManager(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.dice = new Dice();
        this.board = new Board();
        this.currentPlayer = player1; // Player 1 starts first
    }

    /**
     * Returns the player whose turn it is.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Rolls the dice to get a number between 1 and 6.
     */
    public int rollDice() {
        return dice.roll();
    }

    /**
     * Moves the current player based on the roll value.
     * Applies snake or ladder logic after movement.
     *
     * @param rollValue the number rolled by the dice
     * @return the final position after movement (including snake/ladder)
     */
    public int movePlayer(int rollValue) {
        int tentativePos = currentPlayer.getPosition() + rollValue;

        if (tentativePos > 100) {
            lastRawPosition = currentPlayer.getPosition();
            lastFinalPosition = currentPlayer.getPosition();
            return currentPlayer.getPosition(); // no move
        }

        lastRawPosition = tentativePos;

        currentPlayer.moveTo(tentativePos);
        int finalPos = board.checkPosition(currentPlayer.getPosition());
        currentPlayer.moveTo(finalPos);

        lastFinalPosition = finalPos;
        return currentPlayer.getPosition();
    }

    /**
     * Checks whether the current player has won.
     */
    public boolean checkWin() {
        return currentPlayer.getPosition() == 100;
    }

    // Determines if turn should switch.
    public boolean shouldSwitchTurn(int lastRoll) {
        return lastRoll != 6; // If the player rolls a 6, they play again.
    }

    // Switches to the other player's turn.
    public void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Board getBoard() {
        return board;
    }

    // Resets the game state to the beginning.
    public void resetGame() {
        player1.moveTo(1);
        player2.moveTo(1);
        currentPlayer = player1;

    }

    public int getLastRawPosition() {
    return lastRawPosition;
}

public int getLastFinalPosition() {
    return lastFinalPosition;
}

}