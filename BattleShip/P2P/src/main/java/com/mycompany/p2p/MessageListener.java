package com.mycompany.p2p;

/**
 * Listener para mensajes entrantes desde el otro peer.
 * @author Acer
 */
public interface MessageListener {

    void onMessageReceived(String msg);
}
