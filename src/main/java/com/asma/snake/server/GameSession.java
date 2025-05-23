package com.asma.snake.server;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.*;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

// Represents a game session between two players
public class GameSession implements Runnable {

    // Message queues for each player
    private final BlockingQueue<String> redQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> blueQueue = new LinkedBlockingQueue<>();

    private final ClientHandler redHandler, blueHandler; // Handlers for each player
    private final GameManager gm; // Game logic controller

    public GameSession(ClientHandler a, ClientHandler b) {
        this.redHandler = a;
        this.blueHandler = b;
        this.gm = new GameManager(new Player("Player 1", "red"), new Player("Player 2", "blue"));
    }

    @Override
    public void run() {
        try {
            System.out.println("[GameSession] Session started.");
            redHandler.send("MATCHED:red"); // Inform red client they are matched
            blueHandler.send("MATCHED:blue"); // Inform blue client they are matched
            Thread.sleep(500); // Wait a moment before starting

            // Start listening to each client in separate threads
            new Thread(() -> listenToClient(redHandler, redQueue)).start();
            new Thread(() -> listenToClient(blueHandler, blueQueue)).start();

            // Main game loop
            while (!gm.checkWin()) {
                Player current = gm.getCurrentPlayer();
                ClientHandler currentHandler = current.getColor().equals("red") ? redHandler : blueHandler;
                ClientHandler opponentHandler = currentHandler == redHandler ? blueHandler : redHandler;
                BlockingQueue<String> currentQueue = currentHandler == redHandler ? redQueue : blueQueue;
                BlockingQueue<String> opponentQueue = currentHandler == redHandler ? blueQueue : redQueue;

                currentHandler.send("TURN:" + current.getColor()); // Notify whose turn

                while (true) {
                    // Check if opponent exited
                    String opponentMsg = opponentQueue.poll(100, TimeUnit.MILLISECONDS);
                    if ("EXIT".equals(opponentMsg)) {
                        currentHandler.send("EXIT");
                        return;
                    }

                    // Read current player's message
                    String msg = currentQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (msg == null)
                        continue;

                    if ("EXIT".equals(msg)) {
                        opponentHandler.send("EXIT");
                        return;
                    }

                    if (!msg.startsWith("ROLL:"))
                        continue;

                    int roll = Integer.parseInt(msg.split(":", 2)[1]);

                    // Broadcast dice roll
                    String rollMsg = "ROLL:" + current.getColor() + ":" + roll;
                    redHandler.send(rollMsg);
                    blueHandler.send(rollMsg);

                    // Move player and broadcast new position
                    int dest = gm.movePlayer(roll);
                    String moveMsg = "MOVE:" + current.getColor() + ":" + dest;
                    redHandler.send(moveMsg);
                    blueHandler.send(moveMsg);

                    // Check win condition
                    if (gm.checkWin()) {
                        String winMsg = "WIN:" + current.getColor();
                        redHandler.send(winMsg);
                        blueHandler.send(winMsg);
                        return;
                    }

                    // Switch turn if roll != 6
                    if (gm.shouldSwitchTurn(roll)) {
                        gm.switchTurn();
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

    // Continuously reads messages from a client and puts them into a queue
    private void listenToClient(ClientHandler handler, BlockingQueue<String> queue) {
        try {
            String msg;
            while ((msg = handler.receive()) != null) {
                queue.put(msg);
            }
        } catch (IOException | InterruptedException e) {
            try {
                queue.put("EXIT"); // Push exit signal if error occurs
            } catch (InterruptedException ignored) {
            }
        }
    }
}
