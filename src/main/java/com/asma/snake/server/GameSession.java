package com.asma.snake.server;

import java.io.IOException;
import java.net.SocketException;

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
                ClientHandler opponentHandler = turnHandler == redHandler ? blueHandler : redHandler;

                turnHandler.send("TURN:" + current.getColor());

                String line;
                try {
                    line = turnHandler.receive();
                } catch (IOException e) {
                    opponentHandler.send("EXIT");
                    break;
                }

                if (line == null || line.equals("EXIT")) {
                    opponentHandler.send("EXIT");
                    break;
                }

                if (!line.startsWith("ROLL:"))
                    continue;

                int roll = Integer.parseInt(line.split(":", 2)[1]);

                // Broadcast roll and move
                String rollMsg = "ROLL:" + current.getColor() + ":" + roll;
                redHandler.send(rollMsg);
                blueHandler.send(rollMsg);

                int dest = gm.movePlayer(roll);
                String moveMsg = "MOVE:" + current.getColor() + ":" + dest;
                redHandler.send(moveMsg);
                blueHandler.send(moveMsg);

                if (gm.checkWin()) {
                    String winMsg = "WIN:" + current.getColor();
                    redHandler.send(winMsg);
                    blueHandler.send(winMsg);
                    break;
                }

                if (gm.shouldSwitchTurn(roll)) {
                    gm.switchTurn();
                }
            }

            redHandler.close();
            blueHandler.close();

        } catch (Exception e) {
            if (e instanceof SocketException) {
                System.out.println("[GameSession] Player disconnected.");
            } else {
                e.printStackTrace();
            }
        } finally {
            System.out.println("[GameSession] Session ended.");
        }

    }

}
