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
                if (line == null)
                    break;
                if (!line.startsWith("ROLL:"))
                    continue;

                int roll = Integer.parseInt(line.split(":", 2)[1]);

                // Broadcast roll
                String rollMsg = "ROLL:" + currentColor + ":" + roll;
                redHandler.send(rollMsg);
                blueHandler.send(rollMsg);

                // Move player
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

            // Ask for Replay after WIN
            redHandler.send("GAME_OVER:" + gm.getCurrentPlayer().getColor());
            blueHandler.send("GAME_OVER:" + gm.getCurrentPlayer().getColor());

            String redResponse = redHandler.receive();
            String blueResponse = blueHandler.receive();

            if ("REPLAY".equalsIgnoreCase(redResponse)) {
                System.out.println("[GameSession] Red wants to replay.");
                WaitingRoom.enqueue(redHandler);
            } else {
                System.out.println("[GameSession] Red quit.");
                redHandler.close();
            }

            if ("REPLAY".equalsIgnoreCase(blueResponse)) {
                System.out.println("[GameSession] Blue wants to replay.");
                WaitingRoom.enqueue(blueHandler);
            } else {
                System.out.println("[GameSession] Blue quit.");
                blueHandler.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("[GameSession] Session ended.");
        }
    }

}
