package com.asma.snake.server;

import com.asma.snake.chat.ChatServer;
import java.io.IOException;

public class ServerLauncher {
  public static void main(String[] args) throws IOException {
    // 1) start the ChatServer on its own thread
    new Thread(() -> {
      try {
        ChatServer.main(new String[]{});
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    // 2) then start the GameServer on this thread
    GameServer.main(new String[]{});
  }
}
