package com.asma.snake.chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12346;
    // thread-safe set of all client PrintWriters
    private static final Set<PrintWriter> clients = 
        Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("ChatServer listening on " + PORT);
        while (true) {
            Socket sock = server.accept();
            new ChatHandler(sock).start();
        }
    }

    private static class ChatHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        ChatHandler(Socket socket) { this.socket = socket; }

        public void run() {
            try {
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clients.add(out);

                String msg;
                while ((msg = in.readLine()) != null) {
                    // broadcast to everyone
                    for (PrintWriter pw : clients) {
                        pw.println(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(out);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }
}
