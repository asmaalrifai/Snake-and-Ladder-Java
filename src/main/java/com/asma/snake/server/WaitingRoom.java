    package com.asma.snake.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WaitingRoom {
    private static final BlockingQueue<ClientHandler> queue = new LinkedBlockingQueue<>();

    public static void enqueue(ClientHandler handler) {
        if (handler.isClosed())
            return;
        queue.offer(handler);

        while (queue.size() >= 2) {
            ClientHandler a = queue.poll();
            ClientHandler b = queue.poll();

            // Validate both handlers
            if (a == null || b == null || a.isClosed() || b.isClosed()) {
                if (a != null && !a.isClosed())
                    queue.offer(a);
                if (b != null && !b.isClosed())
                    queue.offer(b);
                continue;
            }

            new Thread(new GameSession(a, b)).start();
        }
    }

}
    
