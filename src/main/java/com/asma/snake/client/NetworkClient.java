package com.asma.snake.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

// Handles client-side socket communication with the server
public class NetworkClient {
  private PrintWriter out; // Output stream to server

  public NetworkClient(String host, int port, Consumer<String> onMsg) {
    try {
      Socket s = new Socket(host, port); // Connect to server
      out = new PrintWriter(s.getOutputStream(), true); // Setup output stream
      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); // Setup input stream

      // Start a thread to listen for messages from the server
      new Thread(() -> {
        try {
          String line;
          while ((line = in.readLine()) != null) {
            onMsg.accept(line); // Handle received message
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Sends a message to the server
  public void send(String msg) {
    out.println(msg);
  }

  // Closes the output stream
  public void close() {
    try {
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
