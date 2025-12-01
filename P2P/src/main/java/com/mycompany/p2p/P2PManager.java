package com.mycompany.p2p;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Acer
 */
public class P2PManager {

    private final AtomicReference<ServerPeer> serverRef = new AtomicReference<>();
    private final AtomicReference<ClientPeer> clientRef = new AtomicReference<>();

    private ConnectionListener connectionListener;
    private MessageListener messageListener;

    public P2PManager() {
    }

    // Registro de listeners
    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

     public void startServer(int preferredPort) throws IOException {
        if (serverRef.get() != null) {
            throw new IllegalStateException("Servidor ya esta en ejecucion");
        }
        if (clientRef.get() != null) {
            throw new IllegalStateException("No puedes iniciar un servidor si ya eres cliente");
        }

        ServerPeer server = new ServerPeer(preferredPort, connectionListener, messageListener);
        serverRef.set(server);

        // ServerPeer tiene su propio executor/start()
        server.start();
    }

    /**
     * Inicia la libreria en modo cliente
     * @param serverId 
     * @throws 
     */
    public void startClient(String serverId) throws IOException {
        if (clientRef.get() != null) {
            throw new IllegalStateException("Cliente ya iniciado");
        }
        if (serverRef.get() != null) {
            throw new IllegalStateException("No puedes iniciar un cliente si ya eres servidor");
        }

        String[] parts = serverId.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("ID invalido (Formato esperado: host:port)");
        }

        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        ClientPeer client = new ClientPeer(host, port, connectionListener, messageListener);
        clientRef.set(client);

        // ClientPeer tiene su propio executor/start()
        client.start();
    }

    /**
     * Envía un mensaje al peer conectado.
     * @param msg
     * @throws IOException si ocurre error
     */
    public void sendMessage(String msg) throws IOException {
        ServerPeer server = serverRef.get();
        ClientPeer client = clientRef.get();

        if (server != null && server.isConnected()) {
            server.send(msg);
        } else if (client != null && client.isConnected()) {
            client.send(msg);
        } else {
            throw new IOException("No hay conexion activa para enviar mensajes.");
        }
    }

    public void stop() {
        ServerPeer server = serverRef.getAndSet(null);
        if (server != null) {
            server.stopServer();
        }

        ClientPeer client = clientRef.getAndSet(null);
        if (client != null) {
            client.stopClient();
        }
    }

    public String getServerId() throws UnknownHostException {
        ServerPeer server = serverRef.get();
        if (server != null) {
            return server.getServerId(); // usa el metodo que ya tienes
        }
        return null;
    }
}
