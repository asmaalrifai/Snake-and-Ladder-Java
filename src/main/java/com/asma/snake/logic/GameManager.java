package com.asma.snake.logic;

import com.asma.snake.model.Board;
import com.asma.snake.model.Dice;
import com.asma.snake.model.Player;

public class GameManager {
    private final Player player1;
    private final Player player2;
    private final Dice dice;
    private final Board board;
    private Player currentPlayer;

    public GameManager(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.dice = new Dice();
        this.board = new Board();
        this.currentPlayer = player1;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int rollDice() {
        return dice.roll();
    }

    public int movePlayer(int rollValue) {
        if (!currentPlayer.hasStarted()) {
            if (rollValue == 6) {
                currentPlayer.setStarted(true); // They can start now
            } else {
                return currentPlayer.getPosition(); // They stay at position 1
            }
        } else {
            int nextPosition = currentPlayer.getPosition() + rollValue;
            if (nextPosition > 100) {
                // Must land exactly on 100
                return currentPlayer.getPosition();
            }
            currentPlayer.moveTo(nextPosition);
            int finalPos = board.checkPosition(currentPlayer.getPosition());
            currentPlayer.moveTo(finalPos);
        }
        return currentPlayer.getPosition();
    }

    public boolean checkWin() {
        return currentPlayer.getPosition() == 100;
    }

    public boolean shouldSwitchTurn(int lastRoll) {
        // If the player rolled a 6, they get another turn
        return lastRoll != 6;
    }

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

    public void resetGame() {
        player1.moveTo(1);
        player2.moveTo(1);
        player1.setStarted(false);
        player2.setStarted(false);
        currentPlayer = player1;
    }
}