
package battleship.infrastructure.network;

import battleship.application.services.ComunicacionService;
import battleship.application.services.MessageListener;
import battleship.application.services.ConnectionListener;
import com.mycompany.p2p.ClientPeer;
import com.mycompany.p2p.ServerPeer;
import java.io.IOException;

import java.util.concurrent.atomic.AtomicReference;

public class P2PManager implements ComunicacionService {

    private final AtomicReference<ServerPeer> serverRef = new AtomicReference<>();
    private final AtomicReference<ClientPeer> clientRef = new AtomicReference<>();

    // Usa las interfaces de APPLICATION, no crees nuevas
    private ConnectionListener connectionListener;
    private MessageListener messageListener;

    public P2PManager() {
    }

    @Override
    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    private com.mycompany.p2p.ConnectionListener crearAdaptador() {
        return new com.mycompany.p2p.ConnectionListener() {
            @Override
            public void onServerStarted(String serverId) {
                if (connectionListener != null) {
                    // Llama a onEvent con tipo "serverStarted" y el ID como data
                    connectionListener.onEvent("serverStarted", serverId);
                }
            }
            
            @Override
            public void onClientConnected() {
                if (connectionListener != null) {
                    connectionListener.onEvent("clientConnected", "");
                }
            }
            
            @Override
            public void onConnectedToServer() {
                if (connectionListener != null) {
                    connectionListener.onEvent("connectedToServer", "");
                }
            }
            
            @Override
            public void onPeerDisconnected() {
                if (connectionListener != null) {
                    connectionListener.onEvent("peerDisconnected", "");
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                if (connectionListener != null) {
                    connectionListener.onEvent("error", errorMessage);
                }
            }
        };
    }
    
    @Override
    public void startAsServer() {
        try {
            ServerPeer server = new ServerPeer(5000, 
                crearAdaptador(),  // Usa el adaptador
                new com.mycompany.p2p.MessageListener() {
                    public void onMessageReceived(String message) {
                        if (messageListener != null) {
                            messageListener.onMessage(message);
                        }
                    }
                }
            );
            serverRef.set(server);
            server.start();
        } catch (IOException ex) {
            throw new RuntimeException("Error al iniciar servidor", ex);
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
    @Override
    public void connectToServer(String serverId) throws Exception {
        String[] parts = serverId.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        ClientPeer client = new ClientPeer(host, port,
                crearAdaptador(), // Mismo adaptador
                new com.mycompany.p2p.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                if (messageListener != null) {
                    messageListener.onMessage(message);
                }
            }
        }
        );
        clientRef.set(client);
        client.start();
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
