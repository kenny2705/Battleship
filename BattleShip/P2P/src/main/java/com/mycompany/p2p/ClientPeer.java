package com.mycompany.p2p;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Acer
 */
public class ClientPeer {

    private final String host;
    private final int port;
    private Socket socket;

    private final ConnectionListener connectionListener;
    private final MessageListener messageListener;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    private BufferedWriter writer;

    public ClientPeer(String host, int port,
            ConnectionListener connectionListener,
            MessageListener messageListener) {
        this.host = host;
        this.port = port;
        this.connectionListener = connectionListener;
        this.messageListener = messageListener;
    }

    public void start() {
        executor.submit(() -> {
            try {
                socket = new Socket(host, port);
                running = true;

                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                if (connectionListener != null) {
                    connectionListener.onConnectedToServer();
                }

                listenToServer();

            } catch (IOException ex) {
                if (connectionListener != null) {
                    connectionListener.onError("Error al conectar: " + ex.getMessage());
                }
            }
        });
    }

    private void listenToServer() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String line;

            while (running && (line = in.readLine()) != null) {
                if (messageListener != null) {
                    messageListener.onMessageReceived(line);
                }
            }

        } catch (IOException ex) {
            if (connectionListener != null) {
                connectionListener.onError("I/O cliente: " + ex.getMessage());
            }
        } finally {
            stopClient();
            if (connectionListener != null) {
                connectionListener.onPeerDisconnected();
            }
        }
    }

    public synchronized void send(String msg) throws IOException {
        if (writer == null || socket == null || socket.isClosed()) {
            throw new IOException("No conectado");
        }
        writer.write(msg);
        writer.newLine();
        writer.flush();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void stopClient() {
        running = false;

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ignore) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignore) {
        }

        executor.shutdownNow();
    }
}
