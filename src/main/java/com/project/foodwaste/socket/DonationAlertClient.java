package com.project.foodwaste.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Simple TCP client that can connect to the DonationAlertServer.
 * Used for testing and demonstration purposes.
 */
public class DonationAlertClient {

    private static final Logger logger = LoggerFactory.getLogger(DonationAlertClient.class);

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean connected = false;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;

            String welcome = in.readLine();
            logger.info("Connected to server. Response: {}", welcome);
            return true;
        } catch (IOException e) {
            logger.error("Failed to connect to alert server at {}:{}", host, port, e);
            return false;
        }
    }

    public String readAlert() {
        try {
            if (connected && in != null) {
                return in.readLine();
            }
        } catch (IOException e) {
            logger.error("Error reading from server", e);
            connected = false;
        }
        return null;
    }

    public void sendPing() {
        if (out != null) {
            out.println("PING");
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return connected;
    }
}
