package com.asma.snake.chat;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ChatClient {
    private final PrintWriter out;

    public ChatClient(String host, int port, Consumer<String> onMsg) throws IOException {
        Socket sock = new Socket(host, port);
        out = new PrintWriter(sock.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        // reader thread
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMsg.accept(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void send(String message) {
        out.println(message);
    }
}
