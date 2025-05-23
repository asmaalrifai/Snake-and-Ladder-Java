package com.asma.snake.server;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.*;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

/**
 * Represents a session between two players.
 * Handles turn-based logic, dice rolls, moves, win condition, and communication.
 */
public class GameSession implements Runnable {

    private final BlockingQueue<String> redQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> blueQueue = new LinkedBlockingQueue<>();
    private final ClientHandler redHandler, blueHandler;
    private final GameManager gm;

    public GameSession(ClientHandler a, ClientHandler b) {
        this.redHandler = a;
        this.blueHandler = b;
        this.gm = new GameManager(new Player("Player 1", "red"), new Player("Player 2", "blue"));
    }

    @Override
    public void run() {
        try {
            System.out.println("[GameSession] Session started.");
            redHandler.send("MATCHED:red");
            blueHandler.send("MATCHED:blue");
            Thread.sleep(500);

            // Start listener threads for each client
            new Thread(() -> listenToClient(redHandler, redQueue)).start();
            new Thread(() -> listenToClient(blueHandler, blueQueue)).start();

            while (!gm.checkWin()) {
                Player current = gm.getCurrentPlayer();
                ClientHandler currentHandler = current.getColor().equals("red") ? redHandler : blueHandler;
                ClientHandler opponentHandler = currentHandler == redHandler ? blueHandler : redHandler;
                BlockingQueue<String> currentQueue = (currentHandler == redHandler) ? redQueue : blueQueue;
                BlockingQueue<String> opponentQueue = (currentHandler == redHandler) ? blueQueue : redQueue;

                currentHandler.send("TURN:" + current.getColor());

                // Wait for ROLL or EXIT
                while (true) {
                    String opponentMsg = opponentQueue.poll(100, TimeUnit.MILLISECONDS);
                    if ("EXIT".equals(opponentMsg)) {
                        currentHandler.send("EXIT");
                        return;
                    }

                    String msg = currentQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (msg == null) continue;

                    if ("EXIT".equals(msg)) {
                        opponentHandler.send("EXIT");
                        return;
                    }

                    if (!msg.startsWith("ROLL:")) continue;

                    int roll = Integer.parseInt(msg.split(":", 2)[1]);

                    // Broadcast roll
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
                        return;
                    }

                    if (gm.shouldSwitchTurn(roll)) {
                        gm.switchTurn(); // Do not switch if roll == 6
                    }

                    break;
                }
            }

        } catch (Exception e) {
            if (e instanceof SocketException) {
                System.out.println("[GameSession] Player disconnected.");
            } else {
                e.printStackTrace();
            }
        } finally {
            System.out.println("[GameSession] Session ended.");
            redHandler.close();
            blueHandler.close();
        }
    }

    /** Listens for incoming messages from a client and pushes to queue. */
    private void listenToClient(ClientHandler handler, BlockingQueue<String> queue) {
        try {
            String msg;
            while ((msg = handler.receive()) != null) {
                queue.put(msg);
            }
        } catch (IOException | InterruptedException e) {
            try {
                queue.put("EXIT");
            } catch (InterruptedException ignored) {}
        }
    }
}
