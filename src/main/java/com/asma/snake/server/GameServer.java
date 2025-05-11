package com.asma.snake.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server listening on port 12345...");
        while (true) {
          Socket client = serverSocket.accept();
          new Thread(new ClientHandler(client)).start();
        }
      }
}
