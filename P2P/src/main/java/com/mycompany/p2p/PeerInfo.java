package com.mycompany.p2p;

/**
 *
 * @author Acer
 */
public class PeerInfo {

    private final String host;
    private final int port;
    private final String id;

    public PeerInfo(String host, int port) {
        this.host = host;
        this.port = port;
        this.id = host + ":" + port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
