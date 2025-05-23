package com.asma.snake.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Manages player matchmaking by pairing clients into sessions
public class WaitingRoom {
    private static final BlockingQueue<ClientHandler> queue = new LinkedBlockingQueue<>();

    // Adds a client to the queue and starts a game if two are ready
    public static void enqueue(ClientHandler handler) {
        if (handler.isClosed()) return;

        queue.offer(handler); // Add client to the queue

        while (queue.size() >= 2) {
            ClientHandler a = queue.poll();
            ClientHandler b = queue.poll();

            // Check for valid, connected clients
            if (a == null || b == null || a.isClosed() || b.isClosed()) {
                if (a != null && !a.isClosed()) queue.offer(a); // Requeue if still valid
                if (b != null && !b.isClosed()) queue.offer(b);
                continue;
            }

            // Start a new game session in a new thread
            new Thread(new GameSession(a, b)).start();
        }
    }
}
