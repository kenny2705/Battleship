/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package infraestructura;

import com.mycompany.p2p.ClientPeer;
import com.mycompany.p2p.ConnectionListener;
import com.mycompany.p2p.MessageListener;
import com.mycompany.p2p.ServerPeer;
import java.io.IOException;
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

    // Listener de conexión
    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    // Listener de mensajes
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // ------------------------------
    //  MODO SERVIDOR
    // ------------------------------
    public void startAsServer() {
        try {
            ServerPeer server = new ServerPeer(5000, connectionListener, messageListener);
            serverRef.set(server);

            server.start(); // CORRECTO

        } catch (IOException ex) {
            throw new RuntimeException("Error al iniciar el servidor", ex);
        }
    }

    // Este método es NECESARIO para mostrar el ID
    public String getServerId() {
        ServerPeer server = serverRef.get();
        if (server != null) {
            try {
                return server.getServerId();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // ------------------------------
    //  MODO CLIENTE
    // ------------------------------
    public void connectToServer(String serverId) throws Exception {

        String[] parts = serverId.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato inválido host:port");
        }

        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        ClientPeer client = new ClientPeer(host, port, connectionListener, messageListener);
        clientRef.set(client);

        client.start(); // CORRECTO
    }

    // --------------------------------
    // ENVÍO DE MENSAJES
    // --------------------------------
    public void sendMessage(String msg) throws IOException {
        if (serverRef.get() != null && serverRef.get().isConnected()) {
            serverRef.get().send(msg);
        } else if (clientRef.get() != null && clientRef.get().isConnected()) {
            clientRef.get().send(msg);
        } else {
            throw new IOException("No hay conexión activa.");
        }
    }

    // --------------------------------
    // DETENER
    // --------------------------------
    public void stop() {

        if (serverRef.get() != null) {
            serverRef.get().stopServer();
            serverRef.set(null);
        }

        if (clientRef.get() != null) {
            clientRef.get().stopClient();
            clientRef.set(null);
        }
    }
}
