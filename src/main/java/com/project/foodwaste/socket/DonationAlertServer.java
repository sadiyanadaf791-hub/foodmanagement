package com.project.foodwaste.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DonationAlertServer {

    private static final Logger logger = LoggerFactory.getLogger(DonationAlertServer.class);

    @Value("${socket.server.port:9090}")
    private int port;

    private ServerSocket serverSocket;
    private final CopyOnWriteArrayList<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();
    private final ExecutorService clientThreadPool = Executors.newCachedThreadPool();
    private volatile boolean running = false;
    private Thread serverThread;

    // Store recent alerts for polling by the REST bridge
    private final CopyOnWriteArrayList<String> recentAlerts = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void startServer() {
        serverThread = new Thread(this::run, "SocketServer-Main");
        serverThread.setDaemon(true);
        serverThread.start();
        logger.info("Donation Alert Server starting on port {}", port);
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            logger.info("Donation Alert Server is listening on port {}", port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: {}", clientSocket.getRemoteSocketAddress());
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    connectedClients.add(handler);
                    clientThreadPool.execute(handler);
                } catch (IOException e) {
                    if (running) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Could not start socket server on port {}", port, e);
        }
    }

    public void broadcastAlert(String message) {
        logger.info("Broadcasting alert to {} connected clients", connectedClients.size());
        recentAlerts.add(message);
        // Keep only last 50 alerts
        while (recentAlerts.size() > 50) {
            recentAlerts.remove(0);
        }

        for (ClientHandler client : connectedClients) {
            client.sendMessage(message);
        }
    }

    public CopyOnWriteArrayList<String> getRecentAlerts() {
        return recentAlerts;
    }

    public String getLatestAlert() {
        if (recentAlerts.isEmpty()) return null;
        return recentAlerts.get(recentAlerts.size() - 1);
    }

    public int getConnectedClientCount() {
        return connectedClients.size();
    }

    void removeClient(ClientHandler client) {
        connectedClients.remove(client);
        logger.info("Client disconnected. Remaining clients: {}", connectedClients.size());
    }

    @PreDestroy
    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        clientThreadPool.shutdownNow();
        logger.info("Donation Alert Server stopped");
    }

    /**
     * Inner class to handle each connected client in its own thread.
     */
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final DonationAlertServer server;
        private PrintWriter out;

        ClientHandler(Socket socket, DonationAlertServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("{\"type\":\"CONNECTED\",\"message\":\"Welcome to Food Waste Alert System\"}");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if ("PING".equals(inputLine.trim())) {
                        out.println("{\"type\":\"PONG\"}");
                    }
                }
            } catch (IOException e) {
                // Client disconnected
            } finally {
                server.removeClient(this);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
