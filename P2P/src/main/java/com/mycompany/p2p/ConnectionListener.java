package com.mycompany.p2p;

/**
 *
 * @author Acer
 */
public interface ConnectionListener {
     
    void onServerStarted(String serverId);

    /**
     *  detecta si un segundo jugador se conecto.
     */
    void onClientConnected();

    void onConnectedToServer();

    void onPeerDisconnected();

    /**
     * Error en la librería
     */
    void onError(String errorMessage);
}
