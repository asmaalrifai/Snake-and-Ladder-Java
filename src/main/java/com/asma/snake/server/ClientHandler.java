package com.asma.snake.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

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
                if ("READY".equals(msg)) {
                    WaitingRoom.enqueue(this);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupStreams() throws IOException {
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String message) {
        out.println(message);
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
