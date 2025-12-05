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
import views.Acomodo;
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
        this.p2p = new P2PManager();
        System.out.println("DEBUG: ControlVista instanciado");
    }

    public void setMenuView(Menu menuView) {
        this.menuView = menuView;
    }
    
    private void onConnectedToServer() {
        SwingUtilities.invokeLater(() -> abrirVistaAcomodo());
    }
    // flag para evitar reiniciar el servidor varias veces
    private boolean serverStarted = false;

// helper que muestra el ID (ya lo tenías, pero asegúrate de que esté presente)
    private void mostrarServerId(String serverId) {
        if (menuView != null) {
            JOptionPane.showMessageDialog(menuView,
                    "ID de partida:\n" + serverId,
                    "Servidor iniciado",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

// helper que abre la pantalla de acomodo (ya lo tienes como abrirPantallaAcomodo())
// Si usas otro nombre, asegúrate que exista:
    private void onClientConnectedDoOpen() {
        SwingUtilities.invokeLater(() -> abrirVistaAcomodo());
    }
    /**
     * Inicia servidor (jugador 1). Instala listeners antes de arrancar el
     * servidor.
     */
    public void crearPartida() {

        System.out.println("DEBUG: crearPartida() called. serverStartedFlag=" + serverStarted);
        
        if (serverStarted) {
            JOptionPane.showMessageDialog(menuView,
                    "El servidor ya está iniciado.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        p2p.setConnectionListener(new ConnectionListener() {

            public void onServerStarted(String serverId) {
                System.out.println("SERVER_STARTED: " + serverId);
                mostrarServerId(serverId); // <-- AHORA YA NO ES RECURSIVO
            }

            @Override
            public void onClientConnected() {
                System.out.println("CLIENT_CONNECTED");
                // Ahora el servidor también va a la pantalla Acomodo
                abrirVistaAcomodo();
            }

            @Override
            public void onConnectedToServer() {
            }

            @Override
            public void onPeerDisconnected() {
                System.out.println("PEER_DISCONNECTED");
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("ERROR: " + errorMessage);
                JOptionPane.showMessageDialog(menuView,
                        "Error en servidor:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        p2p.setMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(String msg) {
                System.out.println("MENSAJE SERVER: " + msg);
            }
        });

        p2p.startAsServer();
        serverStarted = true;
    }
    public void unirseAPartida(String serverId) {

        p2p.setConnectionListener(new ConnectionListener() {

            @Override
            public void onServerStarted(String serverId) {
                // NO APLICA EN CLIENTE
            }

            @Override
            public void onClientConnected() {
                // NO SE USA EN CLIENTE (solo servidor)
            }

            @Override
            public void onConnectedToServer() {
                System.out.println("CONECTADO AL SERVIDOR");

                // Ahora el cliente usa el método unificado (antes estaba implícito aquí)
                abrirVistaAcomodo();
            }

            @Override
            public void onPeerDisconnected() {
                System.out.println("PEER_DISCONNECTED");
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("ERROR: " + errorMessage);
                JOptionPane.showMessageDialog(menuView,
                        "Error al conectar:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        p2p.setMessageListener(message -> {
            System.out.println("MENSAJE CLIENTE: " + message);
        });

        try {
            p2p.connectToServer(serverId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(menuView,
                    "Error al conectar. Verifica el ID.",
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirVistaAcomodo() {
        SwingUtilities.invokeLater(() -> {
            if (menuView != null) {
                menuView.setVisible(false);
            }

            // Esta es la pantalla donde arrastran y sueltan las naves
            Acomodo acomodo = new Acomodo(controlJuego, this); // *Ajustar constructor de Acomodo si es necesario*

            acomodo.setLocationRelativeTo(null);
            acomodo.setVisible(true);
        });
    }

// **RENOMBRADO** (Este método debe ser llamado DESPUÉS de que ambos confirmen acomodo)
    private void abrirBatallaPrincipal() {

        // Obtener jugador desde ControlJuego
        Jugador jugador = controlJuego.getJugador();

        // Crear pantalla de juego (La vista principal de batalla)
        // Asegúrate de que esta llamada cumpla con el nuevo constructor de 3 parámetros
        PanelJuego panelJuego = new PanelJuego(jugador, controlJuego, this);

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
