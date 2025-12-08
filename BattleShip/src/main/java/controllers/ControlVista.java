package controllers;

import com.mycompany.p2p.ConnectionListener;
import com.mycompany.p2p.MessageListener;
import infraestructura.P2PManager;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import models.Casilla;
import models.Jugador;
import models.Tablero;
import views.Acomodo;
import views.PanelJuego;

/**
 * CLASE QUE CONTROLA LOS CAMBIOS EN LA INTERFAZ GRAFICA DEL PROYECTO
 * @author Acer
 */
public class ControlVista {

    private final ControlJuego controlJuego;
    private final Map<String, JButton> botonesOponente = new HashMap<String, JButton>();
    private final Map<String, JButton> botonesPropios = new HashMap<String, JButton>();

    private final P2PManager p2p;

    private PanelJuego panelJuegoActivo;

    private ImageIcon iconAgua;
    private ImageIcon iconFallo;
    private ImageIcon iconAcierto;

    private boolean serverStarted = false;
    private Acomodo acomodoView;
    private Jugador jugadorLocal;

    private boolean yoListo = false;
    private boolean rivalListo = false;

    public ControlVista(ControlJuego controlJuego) {
        this.controlJuego = controlJuego;
        this.controlJuego.setControlVista(this);
        this.p2p = new P2PManager();

        cargarIconos();
        instalarListenersP2PGenericos();
    }

    private void cargarIconos() {
        try {
            if (ControlVista.class.getResource("/imagenes/CasillaAgua.png") != null) {
                iconAgua = new ImageIcon(ControlVista.class.getResource("/imagenes/CasillaAgua.png"));
                iconAcierto = new ImageIcon(ControlVista.class.getResource("/imagenes/DisparoAcertado.png"));
                iconFallo = new ImageIcon(ControlVista.class.getResource("/imagenes/DisparoFallido.png"));
            }
        } catch (Exception e) {
            System.err.println("Error cargando iconos: " + e.getMessage());
        }
    }

    private void instalarListenersP2PGenericos() {
        p2p.setConnectionListener(new ConnectionListener() {
            @Override
            public void onServerStarted(String serverId) {
                System.out.println("[P2P] server started: " + serverId);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "ID servidor:\n" + serverId + "\n\nEsperando al Jugador 2..."));
            }

            @Override
            public void onClientConnected() {
                System.out.println("[P2P] client connected");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "¡Jugador 2 conectado! Iniciando acomodo..."));
                abrirAcomodo();
            }

            @Override
            public void onConnectedToServer() {
                System.out.println("[P2P] connected to server");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Conectado al Host.\nEsperando a que el anfitrion termine de acomodar sus naves..."));
            }

            @Override
            public void onPeerDisconnected() {
                System.out.println("[P2P] peer disconnected");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "El otro jugador se ha desconectado.\nLa partida ha finalizado.", "Desconexion", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                });
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("[P2P] error: " + errorMessage);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Error P2P: " + errorMessage));
            }
        });

        p2p.setMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(String msg) {
                System.out.println("[P2P] recibido: " + msg);
                controlJuego.procesarMensajeEntrante(msg);
            }
        });
    }

    private void abrirAcomodo() {
        java.awt.EventQueue.invokeLater(() -> {
            if (acomodoView == null || !acomodoView.isVisible()) {
                Acomodo acomodo = new Acomodo(jugadorLocal, controlJuego, ControlVista.this);
                acomodo.setVisible(true);
                this.acomodoView = acomodo;
            }
        });
    }

    public void crearPartida() {
        if (serverStarted) {
            JOptionPane.showMessageDialog(null, "Servidor ya iniciado");
            return;
        }
        this.jugadorLocal = controlJuego.getJugador();
        jugadorLocal.setNombre("Jugador 1 (Host)");

        p2p.startAsServer();
        serverStarted = true;
    }

    public void unirseAPartida(String serverId) {
        this.jugadorLocal = controlJuego.getJugador();
        jugadorLocal.setNombre("Jugador 2 (Cliente)");
        try {
            p2p.connectToServer(serverId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al conectar: " + ex.getMessage());
        }
    }

    public void enviarMensaje(String msg) throws Exception {
        p2p.sendMessage(msg);
    }
    
    public void terminarPartida(boolean esGanador) {
        SwingUtilities.invokeLater(() -> {
            String mensaje = esGanador
                    ? "¡FELICIDADES! ¡Has hundido toda la flota enemiga!\n¡ERES EL GANADOR!"
                    : "¡DERROTA! Tu flota ha sido destruida.\nMejor suerte la proxima vez.";

            String titulo = esGanador ? "VICTORIA" : "DERROTA";
            int tipo = esGanador ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;

            JOptionPane.showMessageDialog(panelJuegoActivo, mensaje, titulo, tipo);

            System.exit(0);
        });
    }

    public void notificarRivalListo() {
        this.rivalListo = true;
        SwingUtilities.invokeLater(() -> {
            if (!yoListo) {
                mostrarMensaje("¡El anfitrion ha terminado! Ahora es tu turno de acomodar las naves.");
                abrirAcomodo();
            } else {
                verificarInicioJuego();
            }
        });
    }

    public void notificarAcomodoListo(Jugador jugador) {
        try {
            this.yoListo = true;
            enviarMensaje("ACOMODO_LISTO");

            if (acomodoView != null) {
                acomodoView.dispose();
            }

            if (!rivalListo) {
                mostrarMensaje("Has terminado. Esperando a que el oponente acomode sus naves...");
            } else {
                verificarInicioJuego();
            }
        } catch (Exception e) {
            mostrarMensaje("Error al notificar listo: " + e.getMessage());
        }
    }

    private void verificarInicioJuego() {
        if (yoListo && rivalListo) {
            SwingUtilities.invokeLater(() -> {
                if (acomodoView != null) {
                    acomodoView.dispose();
                }

                panelJuegoActivo = new PanelJuego(jugadorLocal, controlJuego, this);
                panelJuegoActivo.setVisible(true);

                javax.swing.Timer t = new javax.swing.Timer(500, e -> {
                    controlJuego.iniciarJuegoReal();
                    ((javax.swing.Timer) e.getSource()).stop();
                });
                t.setRepeats(false);
                t.start();
            });
        }
    }

    public void actualizarTiempo(int segundos) {
        if (panelJuegoActivo != null) {
            panelJuegoActivo.actualizarTemporizador(segundos);
        }
    }

    public void actualizarEstadoTurno(String estado) {
        if (panelJuegoActivo != null) {
            panelJuegoActivo.actualizarEstado(estado);
        }
    }

    public void habilitarTableroOponente(boolean habilitar) {
        SwingUtilities.invokeLater(() -> {
            for (Map.Entry<String, JButton> entry : botonesOponente.entrySet()) {
                JButton btn = entry.getValue();
                if (btn.getIcon() == iconAgua) {
                    btn.setEnabled(habilitar);
                } else {
                    btn.setEnabled(false);
                }
            }
        });
    }

    public void mostrarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, mensaje));
    }

    public void actualizarTableros() {
 
    }

    public void generarTableroPropio(Tablero tablero, JPanel panel, int buttonSize) {
        panel.removeAll();
        botonesPropios.clear();
        int n = tablero.getMedidas();

        tablero.colocarNavesEnCasillas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coord = convertirCoordenada(fila, col);
                Casilla casilla = tablero.getMatrizDeCasillas().get(fila * n + col);

                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(buttonSize, buttonSize));
                btn.setOpaque(true);
                btn.setBorderPainted(true);

                if (casilla.isOcupada()) {
                    btn.setBackground(Color.GRAY);
                    if (casilla.isDañada()) {
                        btn.setIcon(iconAcierto);
                        btn.setBackground(Color.RED);
                    }
                } else {
                    btn.setBackground(new Color(173, 216, 230));
                    if (casilla.isDañada()) {
                        btn.setIcon(iconFallo);
                    } else {
                        btn.setIcon(iconAgua);
                    }
                }

                btn.setEnabled(false);
                panel.add(btn);
                botonesPropios.put(coord, btn);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    public void generarTableroOponente(Tablero tablero, JPanel panel, int buttonSize) {
        panel.removeAll();
        botonesOponente.clear();
        int n = tablero.getMedidas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coord = convertirCoordenada(fila, col);
                Casilla casilla = tablero.getMatrizDeCasillas().get(fila * n + col);
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(buttonSize, buttonSize));
                btn.setActionCommand(coord);
                btn.setOpaque(true);
                btn.setBorderPainted(true);

                if (casilla.isDañada()) {
                    if (casilla.isOcupada()) {
                        btn.setIcon(iconAcierto);
                        btn.setBackground(Color.RED);
                    } else {
                        btn.setIcon(iconFallo);
                        btn.setBackground(Color.DARK_GRAY);
                    }
                    btn.setEnabled(false);
                } else {
                    btn.setIcon(iconAgua);
                    btn.setBackground(new Color(173, 216, 230));
                    btn.setEnabled(false);
                }

                btn.addActionListener(e -> {
                    boolean ok = controlJuego.dispararAOponente(coord);
                    if (!ok) {
                    } else {
                        btn.setEnabled(false);
                    }
                });
                panel.add(btn);
                botonesOponente.put(coord, btn);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    public void actualizarTableroPropio(Tablero tablero) {
        for (Casilla casilla : tablero.getMatrizDeCasillas()) {
            JButton btn = botonesPropios.get(casilla.getCoordenada());
            if (btn != null) {
                if (casilla.isOcupada()) {
                    btn.setBackground(Color.GRAY);
                    if (casilla.isDañada()) {
                        btn.setIcon(iconAcierto);
                        btn.setBackground(Color.RED);
                    }
                } else {
                    btn.setBackground(new Color(173, 216, 230));
                    if (casilla.isDañada()) {
                        btn.setIcon(iconFallo);
                    } else {
                        btn.setIcon(iconAgua);
                    }
                }
            }
        }
    }

    public void actualizarTableroOponente(Tablero tablero) {
        for (Casilla casilla : tablero.getMatrizDeCasillas()) {
            JButton btn = botonesOponente.get(casilla.getCoordenada());
            if (btn != null) {
                if (casilla.isDañada()) {
                    if (casilla.isOcupada()) {
                        btn.setIcon(iconAcierto);
                        btn.setBackground(Color.RED);
                    } else {
                        btn.setIcon(iconFallo);
                        btn.setBackground(Color.DARK_GRAY);
                    }
                    btn.setEnabled(false);
                }
            }
        }
    }

    public void generarTablero(Tablero tablero, JPanel panel) {
        panel.removeAll();
        int n = tablero.getMedidas();
        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coord = convertirCoordenada(fila, col);
                JButton btn = new JButton();
                panel.add(btn);
            }
        }
    }

    public void actualizarBotones(Tablero tablero) {
    }

    private String convertirCoordenada(int fila, int col) {
        return "" + (char) ('A' + fila) + (col + 1);
    }
}
