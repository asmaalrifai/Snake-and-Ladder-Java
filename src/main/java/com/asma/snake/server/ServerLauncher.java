package com.asma.snake.server;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        // Start only the GameServer (chat is removed)
        GameServer.main(new String[]{});
    }
}
