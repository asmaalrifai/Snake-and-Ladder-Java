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
                if ("QUIT".equalsIgnoreCase(msg)) {
                    System.out.println("[ClientHandler] Client quit before match.");
                    close();
                    return;
                } else if ("READY".equalsIgnoreCase(msg)) {
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

    public String receive() throws IOException {
        return in.readLine();
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
