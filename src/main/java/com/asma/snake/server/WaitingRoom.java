package com.asma.snake.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WaitingRoom {
    private static final BlockingQueue<ClientHandler> queue = new LinkedBlockingQueue<>();

    public static void enqueue(ClientHandler handler) {
        queue.offer(handler);
        if (queue.size() >= 2) {
            ClientHandler a = queue.poll();
            ClientHandler b = queue.poll();
            new Thread(new GameSession(a, b)).start();
        }
    }
}
