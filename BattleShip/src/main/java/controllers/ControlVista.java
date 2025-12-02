package controllers;

import infraestructura.P2PManager;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import models.Casilla;
import models.Tablero;
import com.mycompany.p2p.ConnectionListener;
import com.mycompany.p2p.MessageListener;
import models.Jugador;
import views.Menu;
import views.PanelJuego;

/**
 *
 * @author Usuario
 */
public class ControlVista {

    private final ControlJuego controlJuego;
    private final Map<String, JButton> botones;

    private Menu menuView;
    private final P2PManager p2p;

    public ControlVista(ControlJuego controlJuego) {
        this.controlJuego = controlJuego;
        this.botones = new HashMap<>();
        this.p2p = new P2PManager(); // asegúrate que este P2PManager está en paquete infraestructura
    }

    public void setMenuView(Menu menuView) {
        this.menuView = menuView;
    }

    /**
     * Inicia servidor (jugador 1). Instala listeners antes de arrancar el
     * servidor.
     */
    public void crearPartida() {

        // Listener con tu interfaz real
        p2p.setConnectionListener(new ConnectionListener() {
            public void onEvent(String type, String data) {
                System.out.println("SERVER EVENT: " + type + " / " + data);

                switch (type) {

                    case "SERVER_STARTED":
                        // data = serverId
                        break;

                    case "CLIENT_CONNECTED":
                        SwingUtilities.invokeLater(() -> abrirPantallaAcomodo());
                        break;

                    case "ERROR":
                        System.err.println("ERROR SERVIDOR: " + data);
                        break;
                }
            }

            @Override
            public void onServerStarted(String serverId) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onClientConnected() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onConnectedToServer() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onPeerDisconnected() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onError(String errorMessage) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });

        // Listener de mensajes
        p2p.setMessageListener(new MessageListener() {
            public void onMessage(String message) {
                System.out.println("MENSAJE SERVER: " + message);
            }

            @Override
            public void onMessageReceived(String msg) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });

        // Iniciar servidor
        p2p.startAsServer();

        // Mostrar ID al jugador
        String id = p2p.getServerId();
        JOptionPane.showMessageDialog(menuView,
                "ID de partida:\n" + id + "\nCompártelo con el jugador 2.",
                "Servidor iniciado",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void unirseAPartida(String serverId) {

        p2p.setConnectionListener(new ConnectionListener() {
            public void onEvent(String type, String data) {

                switch (type) {

                    case "CONNECTED_TO_SERVER":
                        System.out.println("CLIENTE: Conectado al servidor");
                        SwingUtilities.invokeLater(() -> abrirPantallaAcomodo());
                        break;

                    case "ERROR":
                        JOptionPane.showMessageDialog(menuView,
                                "No se pudo conectar al servidor:\n" + data,
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        break;
                }
            }

            @Override
            public void onServerStarted(String serverId) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onClientConnected() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onConnectedToServer() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onPeerDisconnected() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onError(String errorMessage) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });

        p2p.setMessageListener(new MessageListener() {
            public void onMessage(String message) {
                System.out.println("MENSAJE CLIENTE: " + message);
            }

            @Override
            public void onMessageReceived(String msg) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
        try {
            p2p.connectToServer(serverId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(menuView,
                    "Error al conectar. Verifica el ID.",
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private void abrirPantallaAcomodo() {

        // Obtener jugador desde ControlJuego
        Jugador jugador = controlJuego.getJugador();

        // Crear pantalla de juego usando el controlJuego correcto
        PanelJuego panelJuego = new PanelJuego(jugador, controlJuego);

        panelJuego.setLocationRelativeTo(null);
        panelJuego.setVisible(true);

        if (menuView != null) {
            menuView.dispose();
        }
    }
    public void generarTablero(Tablero tablero, JPanel panelTablero) {
        int n = tablero.getMedidas();
        int buttonSize = 60;
        panelTablero.removeAll();
        botones.clear();

        ImageIcon iconAgua = new ImageIcon(getClass().getResource("/imagenes/CasillaAgua.png"));
        ImageIcon iconAcierto = new ImageIcon(getClass().getResource("/imagenes/DisparoAcertado.png"));
        //ImageIcon iconHundido = new ImageIcon(getClass().getResource("/imagenes/NaveHundida.png"));
        ImageIcon iconFallo = new ImageIcon(getClass().getResource("/imagenes/DisparoFallido.png"));

        tablero.colocarNavesEnCasillas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coordenada = convertirCoordenada(fila, col);
                Casilla casilla = tablero.getMatrizDeCasillas().get(fila * n + col);

                JButton btn = new JButton();
                btn.setBounds(col * buttonSize, fila * buttonSize, buttonSize, buttonSize);
                btn.setActionCommand(coordenada);

                btn.setIcon(casilla.isDañada() ? (casilla.isOcupada() ? iconAcierto : iconFallo) : iconAgua);
                btn.setEnabled(!casilla.isDañada());
                btn.addActionListener(e -> {
                    if (!casilla.isDañada()) {
                        // Actualiza el modelo y obtiene resultado
                        controlJuego.realizarDisparo(tablero, coordenada);
                    }
                });

                panelTablero.add(btn);
                botones.put(coordenada, btn);
            }
        }
        panelTablero.setPreferredSize(new Dimension(n * buttonSize, n * buttonSize));
        panelTablero.revalidate();
        panelTablero.repaint();

//        panelTablero.setPreferredSize(new java.awt.Dimension(n * buttonSize, n * buttonSize));
//        panelTablero.revalidate();
//        panelTablero.repaint();
    }

    public void actualizarBotones(Tablero tablero) {
        ImageIcon iconAgua = new ImageIcon(getClass().getResource("/imagenes/CasillaAgua.png"));
        ImageIcon iconAcierto = new ImageIcon(getClass().getResource("/imagenes/DisparoAcertado.png"));
        ImageIcon iconFallo = new ImageIcon(getClass().getResource("/imagenes/DisparoFallido.png"));

        for (Casilla casilla : tablero.getMatrizDeCasillas()) {
            JButton btn = botones.get(casilla.getCoordenada());
            if (btn != null) {
                btn.setIcon(casilla.isDañada() ? (casilla.isOcupada() ? iconAcierto : iconFallo) : iconAgua);
                btn.setEnabled(!casilla.isDañada());
                
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setOpaque(false);
            }
        }
    }

    public Map<String, JButton> getBotones() {
        return botones;
    }

    //Pasa las coordenadas de numeros a letra y numero
    private String convertirCoordenada(int fila, int col) {
        char letra = (char) ('A' + fila);
        return letra + String.valueOf(col + 1);
    }

}
