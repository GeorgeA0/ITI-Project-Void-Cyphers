package com.iwish.server.net;

import com.iwish.server.db.Database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * i-Wish server: Start/Stop (spec item 11) plus accepting and dispatching
 * client connections (spec item 13). Each accepted socket is handed to a
 * {@link ClientHandler} running on its own pooled thread so multiple
 * clients can be connected at the same time.
 */
public class Server {

    private final int port;
    private final SessionManager sessionManager = new SessionManager();
    private final ExecutorService pool = Executors.newCachedThreadPool();

    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;

    public Server(int port) {
        this.port = port;
    }

    /** Opens the DB connection, binds the port and starts accepting clients. */
    public synchronized void start() {
        if (running) {
            return;
        }
        Database.getConnection(); // connect + create schema up front, fail fast if that's broken
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("Could not bind to port " + port, e);
        }
        running = true;
        acceptThread = new Thread(this::acceptLoop, "iwish-accept-loop");
        acceptThread.start();
        System.out.println("[Server] i-Wish server listening on port " + port);
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                pool.submit(new ClientHandler(clientSocket, sessionManager));
            } catch (IOException e) {
                if (running) {
                    System.out.println("[Server] Accept failed: " + e.getMessage());
                }
                // if !running, this is just the socket closing during stop() - ignore
            }
        }
    }

    /** Stops accepting new clients and shuts down the worker pool. */
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
        pool.shutdownNow();
        System.out.println("[Server] Stopped.");
    }

    public boolean isRunning() {
        return running;
    }
}
