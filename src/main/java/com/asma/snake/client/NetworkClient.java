package com.asma.snake.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {
  private PrintWriter out;
  public NetworkClient(String host, int port, Consumer<String> onMsg) {
    try {
      Socket s = new Socket(host, port);
      out = new PrintWriter(s.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      out.println("READY");
      new Thread(() -> {
        try {
          String line;
          while ((line = in.readLine()) != null) {
            onMsg.accept(line);
          }
        } catch (IOException e) { e.printStackTrace(); }
      }).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void send(String msg) {
    out.println(msg);
  }
}