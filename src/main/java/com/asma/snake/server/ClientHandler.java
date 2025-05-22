package com.asma.snake.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean closed = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            setupStreams();
            out.println("CONNECT");

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[ClientHandler] Received: " + msg);
                if ("READY".equalsIgnoreCase(msg)) {
                    if (!isClosed()) {
                        WaitingRoom.enqueue(this);
                    }
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String message) {
        out.println(message);
    }

    // ✅ this method should stay outside the run() method
    public String receive() throws IOException {
        return in.readLine();
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
