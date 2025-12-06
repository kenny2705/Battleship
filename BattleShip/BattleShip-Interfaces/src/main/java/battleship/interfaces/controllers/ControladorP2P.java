// controllers.ControladorP2P.java
package controllers;

import battleship.application.ControlJuego;
import battleship.application.services.ComunicacionService;
import battleship.application.services.ConnectionListener;
import battleship.application.services.MessageListener;
import battleship.interfaces.controllers.ControlVista;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ControladorP2P {
    
    private final ComunicacionService comunicacionService;
    private ControlVista controlVista;
    private ControlJuego controlJuego;
    
    public ControladorP2P(ComunicacionService comunicacionService) {
        this.comunicacionService = comunicacionService;
    }
    
    public void setControlVista(ControlVista controlVista) {
        this.controlVista = controlVista;
    }
    
    public void setControlJuego(ControlJuego controlJuego) {
        this.controlJuego = controlJuego;
    }
    
    public void crearPartida() {
        String nombre = JOptionPane.showInputDialog(
            "Introduce tu nombre:",
            "Crear partida"
        );
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            configurarListeners();
            controlJuego.iniciarComoServidor(nombre.trim());
        }
    }
    
    public void unirseAPartida(String serverId) {
        String nombre = JOptionPane.showInputDialog(
            "Introduce tu nombre:",
            "Unirse a partida"
        );
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            configurarListeners();
            controlJuego.conectarComoCliente(serverId.trim(), nombre.trim());
        }
    }
    
    private void configurarListeners() {
        comunicacionService.setConnectionListener(new ConnectionListener() {
            @Override
            public void onEvent(String type, String data) {
                switch (type) {
                    case "serverStarted":
                        System.out.println("Servidor iniciado. ID: " + data);
                        mostrarMensaje("Servidor creado", 
                            "ID para compartir: " + data + "\nEsperando conexión...");
                        break;
                    case "clientConnected":
                        System.out.println("Cliente conectado al servidor");
                        // Cuando un cliente se conecta (soy servidor)
                        break;
                    case "connectedToServer":
                        System.out.println("Conectado al servidor oponente");
                        // Cuando me conecto a un servidor (soy cliente)
                        break;
                    case "peerDisconnected":
                        System.out.println("Oponente desconectado");
                        mostrarMensaje("Conexión perdida", "El oponente se ha desconectado");
                        break;
                    case "error":
                        System.err.println("Error: " + data);
                        mostrarMensaje("Error de conexión", data);
                        break;
                }
            }
        });
        
        comunicacionService.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(String message) {
                System.out.println("Mensaje recibido: " + message);
                // Los mensajes del juego los procesa ControlJuego
            }
        });
    }
    
    private void mostrarMensaje(String titulo, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    public void desconectar() {
        if (comunicacionService != null) {
            comunicacionService.stop();
        }
    }
}