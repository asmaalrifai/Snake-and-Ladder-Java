package com.asma.snake.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Handles socket communication from client to server.
 * Reads incoming messages on a separate thread and sends data to the server.
 */
public class NetworkClient {
    private PrintWriter out;

    public NetworkClient(String host, int port, Consumer<String> onMsg) {
        try {
            Socket s = new Socket(host, port);
            out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // Listen for server messages in a new thread
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Sends a message to the server. */
    public void send(String msg) {
        out.println(msg);
    }

    /** Closes the output stream. */
    public void close() {
        try {
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
