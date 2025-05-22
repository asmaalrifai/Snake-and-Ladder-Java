    package com.asma.snake.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(40000);
        System.out.println("[GameServer] Listening on port 40000...");
        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("[GameServer] New client connected.");
            new Thread(new ClientHandler(client)).start();
        }
    }
}
    
