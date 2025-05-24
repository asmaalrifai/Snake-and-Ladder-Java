package com.asma.snake.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Manages player matchmaking by pairing clients into sessions
public class WaitingRoom {
    private static final BlockingQueue<ClientHandler> queue = new LinkedBlockingQueue<>();

    // Adds a client to the queue and starts a game if two are ready
    public static void enqueue(ClientHandler handler) {
        if (handler.isClosed())
            return;

        queue.offer(handler); // Add client to the queue

        while (queue.size() >= 2) {
            ClientHandler a = queue.poll();
            ClientHandler b = queue.poll();

            // Check for valid, connected clients by pinging the socket
            if (a == null || !isAlive(a)) {
                if (a != null)
                    a.close(); // Cleanup
                if (b != null && isAlive(b))
                    queue.offer(b);
                continue;
            }

            if (b == null || !isAlive(b)) {
                if (b != null)
                    b.close();
                if (a != null && isAlive(a))
                    queue.offer(a);
                continue;
            }

            new Thread(new GameSession(a, b)).start();
        }

    }

    private static boolean isAlive(ClientHandler handler) {
        return handler != null && !handler.isClosed();
    }

}
