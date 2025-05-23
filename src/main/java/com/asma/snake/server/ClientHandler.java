package com.asma.snake.server;

import java.io.*;
import java.net.Socket;

// Handles communication with a single client on the server
public class ClientHandler implements Runnable {
    private final Socket socket; // Client socket
    private BufferedReader in;   // Input stream from client
    private PrintWriter out;     // Output stream to client
    private boolean closed = false; // Flag to check if handler is closed

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            setupStreams(); // Initialize I/O streams
            out.println("CONNECT"); // Notify client that connection is successful

            String msg;
            while ((msg = in.readLine()) != null) { // Listen for client messages
                System.out.println("[ClientHandler] Received: " + msg);
                if ("READY".equalsIgnoreCase(msg) && !isClosed()) {
                    WaitingRoom.enqueue(this); // Add to waiting room for matchmaking
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sets up input/output streams
    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    // Sends a message to the client
    public void send(String message) {
        out.println(message);
    }

    // Receives a message from the client
    public String receive() throws IOException {
        return in.readLine();
    }

    // Checks if the client connection is closed
    public boolean isClosed() {
        return closed;
    }

    // Closes the connection safely
    public void close() {
        closed = true;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}
