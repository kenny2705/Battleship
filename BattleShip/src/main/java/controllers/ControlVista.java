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
import models.enums.ResultadoDisparo;
import views.Acomodo;
import views.PanelJuego;

/**
 *
 * @author Acer
 */
public class ControlVista {

    private final ControlJuego controlJuego;
    // Mapas separados para controlar los botones de cada tablero independientemente
    private final Map<String, JButton> botonesOponente = new HashMap<String, JButton>();
    private final Map<String, JButton> botonesPropios = new HashMap<String, JButton>();

    private final P2PManager p2p;

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

                // HOST: Abre su ventana de acomodo en cuanto llega el cliente
                abrirAcomodo();
            }

            @Override
            public void onConnectedToServer() {
                System.out.println("[P2P] connected to server");
                // CLIENTE: NO ABRE ACOMODO AÚN. Espera a que el Host termine (Flujo secuencial).
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Conectado al Host.\nEsperando a que el anfitrión termine de acomodar sus naves..."));
            }

            @Override
            public void onPeerDisconnected() {
                System.out.println("[P2P] peer disconnected");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "El otro jugador se desconectó."));
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

    // HOST
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

    // CLIENTE
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

    // --- LÓGICA DE FLUJO SECUENCIAL ---
    public void notificarRivalListo() {
        this.rivalListo = true;
        System.out.println("DEBUG: Rival Listo recibido. YoListo=" + yoListo);

        SwingUtilities.invokeLater(() -> {
            if (!yoListo) {
                // CASO CLIENTE: El Host ya terminó, ahora es MI turno.
                mostrarMensaje("¡El anfitrión ha terminado! Ahora es tu turno de acomodar las naves.");
                abrirAcomodo();
            } else {
                // CASO HOST: Yo ya estaba listo esperando, y el Cliente acaba de terminar.
                verificarInicioJuego();
            }
        });
    }

    public void notificarAcomodoListo(Jugador jugador) {
        try {
            this.yoListo = true;
            enviarMensaje("ACOMODO_LISTO");

            // Cerrar mi pantalla de acomodo
            if (acomodoView != null) {
                acomodoView.dispose();
            }

            if (!rivalListo) {
                // CASO HOST: Terminé primero. Esperar al cliente.
                mostrarMensaje("Has terminado. Esperando a que el oponente acomode sus naves...");
            } else {
                // CASO CLIENTE: El Host ya estaba listo, iniciamos.
                verificarInicioJuego();
            }
        } catch (Exception e) {
            mostrarMensaje("Error al notificar listo: " + e.getMessage());
        }
    }

    private void verificarInicioJuego() {
        if (yoListo && rivalListo) {
            System.out.println("DEBUG: Ambos listos. Iniciando PanelJuego...");
            SwingUtilities.invokeLater(() -> {
                if (acomodoView != null) {
                    acomodoView.dispose();
                }

                // Abrir pantalla de juego con ambos tableros
                PanelJuego panelJuego = new PanelJuego(jugadorLocal, controlJuego, this);
                panelJuego.setVisible(true);
            });
        }
    }

    public void mostrarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, mensaje));
    }

    public void actualizarTableros() {
        // Este método es genérico, pero PanelJuego llamará a los específicos de abajo
    }

    // --- GENERACIÓN DE TABLEROS (CORREGIDO: VISIBILIDAD Y DIMENSIONES) ---
    // 1. TABLERO PROPIO (IZQUIERDA): VISIBLE
    public void generarTableroPropio(Tablero tablero, JPanel panel, int buttonSize) {
        panel.removeAll();
        botonesPropios.clear();
        int n = tablero.getMedidas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coord = convertirCoordenada(fila, col);
                Casilla casilla = tablero.getMatrizDeCasillas().get(fila * n + col);

                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(buttonSize, buttonSize)); // Forzar tamaño

                // CORRECCIÓN VISUAL: Forzar opacidad para que el color de fondo se vea
                btn.setOpaque(true);
                btn.setBorderPainted(true);

                // LÓGICA VISUAL PROPIA: Se muestran las naves (Color.GRAY)
                if (casilla.isOcupada()) {
                    btn.setBackground(Color.GRAY);
                    if (casilla.isDañada()) {
                        btn.setIcon(iconAcierto);
                    }
                } else {
                    btn.setBackground(new Color(173, 216, 230)); // Azul claro
                    if (casilla.isDañada()) {
                        btn.setIcon(iconFallo);
                    } else {
                        btn.setIcon(iconAgua);
                    }
                }

                btn.setEnabled(false); // No interactivo
                panel.add(btn);
                botonesPropios.put(coord, btn);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    // 2. TABLERO OPONENTE (DERECHA): NIEBLA DE GUERRA
    public void generarTableroOponente(Tablero tablero, JPanel panel, int buttonSize) {
        panel.removeAll();
        botonesOponente.clear();
        int n = tablero.getMedidas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coord = convertirCoordenada(fila, col);
                Casilla casilla = tablero.getMatrizDeCasillas().get(fila * n + col);
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(buttonSize, buttonSize)); // Forzar tamaño
                btn.setActionCommand(coord);

                // CORRECCIÓN VISUAL
                btn.setOpaque(true);
                btn.setBorderPainted(true);

                // LÓGICA VISUAL RIVAL: Naves ocultas hasta que son impactadas
                if (casilla.isDañada()) {
                    if (casilla.isOcupada()) {
                        btn.setIcon(iconAcierto); // Hit
                        btn.setBackground(Color.RED); // Opcional: fondo rojo para impacto
                    } else {
                        btn.setIcon(iconFallo); // Miss
                        btn.setBackground(Color.BLUE); // Opcional: fondo azul oscuro para fallo
                    }
                    btn.setEnabled(false);
                } else {
                    btn.setIcon(iconAgua); // Se ve como agua aunque haya nave
                    btn.setBackground(new Color(173, 216, 230));
                    btn.setEnabled(true);
                }

                btn.addActionListener(e -> {
                    boolean ok = controlJuego.dispararAOponente(coord);
                    if (!ok) {
                        // Feedback opcional
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

    // Actualiza botones PROPIOS (cuando me disparan)
    public void actualizarTableroPropio(Tablero tablero) {
        for (Casilla casilla : tablero.getMatrizDeCasillas()) {
            JButton btn = botonesPropios.get(casilla.getCoordenada());
            if (btn != null) {
                if (casilla.isOcupada()) {
                    btn.setBackground(Color.GRAY);
                    if (casilla.isDañada()) {
                        btn.setIcon(iconAcierto);
                    }
                } else {
                    btn.setBackground(new Color(173, 216, 230));
                    if (casilla.isDañada()) {
                        btn.setIcon(iconFallo);
                    } else {
                        btn.setIcon(iconAgua);
                    }
                }
                btn.setOpaque(true); // Re-afirmar opacidad
                btn.repaint(); // CORRECCIÓN: Forzar repintado inmediato
            }
        }
    }

    // Actualiza botones RIVALES (cuando yo disparo)
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
                        btn.setBackground(Color.BLUE);
                    }
                    btn.setEnabled(false);
                }
                btn.setOpaque(true); // Re-afirmar opacidad
                btn.repaint(); // CORRECCIÓN: Forzar repintado inmediato
            }
        }
    }

    // Método legacy para Acomodo (mantener)
    public void generarTablero(Tablero tablero, JPanel panel) {
        panel.removeAll();
        int n = tablero.getMedidas();
        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coord = convertirCoordenada(fila, col);
                JButton btn = new JButton();
                // Lógica simple para acomodo (reutiliza visualización si se desea)
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
