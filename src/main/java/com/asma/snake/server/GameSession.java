package com.asma.snake.server;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

public class GameSession implements Runnable {
    private final ClientHandler redHandler, blueHandler;
    private final GameManager gm;

    public GameSession(ClientHandler a, ClientHandler b) {
        this.redHandler = a;
        this.blueHandler = b;
        Player redPlayer = new Player("Player 1", "red");
        Player bluePlayer = new Player("Player 2", "blue");
        this.gm = new GameManager(redPlayer, bluePlayer);
    }

    @Override
    public void run() {
        try {
            System.out.println("[GameSession] Session started.");
            redHandler.send("MATCHED:red");
            blueHandler.send("MATCHED:blue");
            Thread.sleep(500);

            while (!gm.checkWin()) {
                Player current = gm.getCurrentPlayer();
                ClientHandler turnHandler = current.getColor().equals("red") ? redHandler : blueHandler;
                String currentColor = current.getColor();

                turnHandler.send("TURN:" + currentColor);
                String line = turnHandler.receive();
                if (line == null) break;
                if (!line.startsWith("ROLL:")) continue;

                int roll = Integer.parseInt(line.split(":", 2)[1]);
                int dest = gm.movePlayer(roll);

                String moveMsg = "MOVE:" + currentColor + ":" + dest;
                redHandler.send(moveMsg);
                blueHandler.send(moveMsg);

                if (gm.checkWin()) {
                    String winMsg = "WIN:" + currentColor;
                    redHandler.send(winMsg);
                    blueHandler.send(winMsg);
                    break;
                }

                if (gm.shouldSwitchTurn(roll)) {
                    gm.switchTurn();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redHandler.close();
            blueHandler.close();
            System.out.println("[GameSession] Session ended.");
        }
    }
}
