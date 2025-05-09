package com.asma.snake.server;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

public class GameSession implements Runnable {
    private final ClientHandler redHandler, blueHandler;
    private final GameManager gm;

    public GameSession(ClientHandler a, ClientHandler b) {
        this.redHandler  = a;
        this.blueHandler = b;
        // Create two Player objects matching the colors:
        Player redPlayer  = new Player("Player 1", "red");
        Player bluePlayer = new Player("Player 2", "blue");
        this.gm = new GameManager(redPlayer, bluePlayer);
    }

    @Override
    public void run() {
        try {
            // Tell each client their color
            redHandler.send("MATCHED:red");
            blueHandler.send("MATCHED:blue");

            // Game loop
            while (!gm.checkWin()) {
                // Whose turn?
                Player current = gm.getCurrentPlayer();
                ClientHandler turnHandler = current.getColor().equals("red")
                                            ? redHandler : blueHandler;

                turnHandler.send("TURN:" + current.getColor());

                // Wait for ROLL:x
                String line = turnHandler.receive();
                if (line == null) break;
                if (!line.startsWith("ROLL:")) continue;

                int roll = Integer.parseInt(line.split(":", 2)[1]);
                int dest = gm.movePlayer(roll);

                // Broadcast the move
                String moveMsg = "MOVE:" + current.getColor() + ":" + dest;
                redHandler.send(moveMsg);
                blueHandler.send(moveMsg);

                // Check win
                if (gm.checkWin()) {
                    String winMsg = "WIN:" + current.getColor();
                    redHandler.send(winMsg);
                    blueHandler.send(winMsg);
                    break;
                }

                // Switch turn if needed
                if (gm.shouldSwitchTurn(roll)) {
                    gm.switchTurn();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redHandler.close();
            blueHandler.close();
        }
    }
}
