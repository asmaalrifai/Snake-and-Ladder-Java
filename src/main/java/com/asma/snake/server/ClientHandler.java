package com.asma.snake.server;

import java.io.*;
import java.net.Socket;

/**
 * Handles communication with one connected client on the server side.
 */
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
            out.println("CONNECT"); // Inform client that connection is established

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[ClientHandler] Received: " + msg);
                if ("READY".equalsIgnoreCase(msg) && !isClosed()) {
                    WaitingRoom.enqueue(this);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Sets up input/output streams. */
    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /** Sends a message to the client. */
    public void send(String message) {
        out.println(message);
    }

    /** Receives a message from the client. */
    public String receive() throws IOException {
        return in.readLine();
    }

    public boolean isClosed() {
        return closed;
    }

    /** Closes the socket safely. */
    public void close() {
        closed = true;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}
