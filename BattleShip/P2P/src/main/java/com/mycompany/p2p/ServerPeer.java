package com.mycompany.p2p;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Acer
 */
public class ServerPeer {

    private final int port;
    private final ConnectionListener connectionListener;
    private final MessageListener messageListener;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private volatile boolean running = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ServerPeer(int port,
            ConnectionListener connectionListener,
            MessageListener messageListener) {
        this.port = port;
        this.connectionListener = connectionListener;
        this.messageListener = messageListener;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        executor.submit(() -> {
            try {
                if (connectionListener != null) {
                    connectionListener.onServerStarted(getServerId());
                }

                clientSocket = serverSocket.accept();

                if (connectionListener != null) {
                    connectionListener.onClientConnected();
                }

                listenToClient();

            } catch (IOException ex) {
                if (connectionListener != null) {
                    connectionListener.onError("Error en servidor: " + ex.getMessage());
                }
            } finally {
                stopServer();
            }
        });
    }

    private void listenToClient() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {

            String line;
            while (running && (line = in.readLine()) != null) {
                if (messageListener != null) {
                    messageListener.onMessageReceived(line);
                }
            }
        } catch (IOException ex) {
            if (connectionListener != null) {
                connectionListener.onError("Error leyendo cliente: " + ex.getMessage());
            }
        } finally {
            if (connectionListener != null) {
                connectionListener.onPeerDisconnected();
            }
        }
    }

    public synchronized void send(String msg) throws IOException {
        if (clientSocket == null || clientSocket.isClosed()) {
            throw new IOException("No hay cliente conectado");
        }

        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(clientSocket.getOutputStream()));

        out.write(msg);
        out.newLine();
        out.flush();
    }

    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public void stopServer() {
        running = false;

        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException ignore) {
        }

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignore) {
        }

        executor.shutdownNow();
    }

    public String getServerId() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress() + ":" + port;
    }
}
