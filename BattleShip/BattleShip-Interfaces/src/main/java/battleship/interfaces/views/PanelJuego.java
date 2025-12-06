package battleship.interfaces.views;

import battleship.application.ControlJuego;
import battleship.interfaces.controllers.PartidaController;
import battleship.interfaces.controllers.ControlVista;
import battleship.domain.enums.ResultadoDisparo;
import battleship.domain.model.Tablero;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class PanelJuego extends javax.swing.JFrame
        implements PartidaController.ViewUpdateListener,
        PartidaController.VistaTableroListener {  // ✅ IMPLEMENTA NUEVA INTERFAZ

    private static final Logger logger = Logger.getLogger(PanelJuego.class.getName());

    private final PartidaController partidaController;
    private final ControlVista controlVista;

    private javax.swing.JLabel lblEstadoTurno;
    private javax.swing.JLabel lblEstadoPartida;

    public PanelJuego(PartidaController partidaController) {
        this.partidaController = partidaController;

        initComponents();
        inicializarComponentesAdicionales();

        // ✅ Obtener ControlJuego
        ControlJuego controlJuego = partidaController.getControlJuego();

        // ✅ Registrar listeners
        partidaController.addViewUpdateListener(this);
        partidaController.addVistaTableroListener(this);

        logger.info("PanelJuego creado para: " + partidaController.getNombreJugador());

        // ✅ Crear ControlVista
        this.controlVista = new ControlVista(
                crearCeldaClickListener(partidaController, controlJuego),
                panelTablero,
                controlJuego,
                partidaController // ← Pasar PartidaController también
        );

        inicializarTablero();
        actualizarInterfaz();
    }

    private ControlVista.CeldaClickListener crearCeldaClickListener(
            PartidaController controller, ControlJuego controlJuego) {

        return (fila, col) -> {
            // ✅ Las coordenadas ya son numéricas (0-based)
            System.out.println("Click en celda: fila=" + fila + ", col=" + col);

            if (controlJuego.isPartidaIniciada()) {
                // ✅ Modo online: convertir a formato "fila,columna" (1-based)
                String coordenada = (fila + 1) + "," + (col + 1);
                controlJuego.realizarDisparoOnline(coordenada);
            } else {
                // ✅ Modo local: usar PartidaController directamente con coordenadas numéricas
                controller.procesarDisparo(fila, col);
            }

            // Actualizar interfaz
            SwingUtilities.invokeLater(() -> actualizarInterfaz());
        };
    }

    private void inicializarTablero() {
        // Inicializar tablero con las dimensiones correctas
        Tablero tableroOponente = partidaController.getTableroOponente();
        if (tableroOponente != null) {
            controlVista.generarTablero(tableroOponente);
        }
    }

    private void inicializarComponentesAdicionales() {
        lblEstadoTurno = new javax.swing.JLabel();
        lblEstadoPartida = new javax.swing.JLabel();

        lblEstadoTurno.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblEstadoTurno.setForeground(java.awt.Color.GREEN);

        lblEstadoPartida.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblEstadoPartida.setForeground(java.awt.Color.BLUE);

        getContentPane().add(lblEstadoTurno, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 150, 400, 30));
        getContentPane().add(lblEstadoPartida, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 200, 400, 30));
    }

    private void onCeldaClick(int fila, int col) {
        System.out.println("🖱️  PanelJuego.onCeldaClick(" + fila + "," + col + ")");

        if (!partidaController.esPartidaActiva()) {
            JOptionPane.showMessageDialog(this,
                    "La partida ha terminado. ¡"
                    + partidaController.getNombreGanador() + " es el ganador!");
            return;
        }

        if (!partidaController.esTurnoDelJugador()) {
            JOptionPane.showMessageDialog(this, "No es tu turno.");
            return;
        }

        try {
            ResultadoDisparo resultado = partidaController.procesarDisparo(fila, col);

            if (resultado != null) {
                String mensaje = switch (resultado) {
                    case AGUA ->
                        "¡Agua! Turno pasado al oponente";
                    case IMPACTO ->
                        "¡Impacto! Sigues disparando";
                    case HUNDIDO ->
                        "¡Hundido! Sigues disparando";
                    case REPETIDO ->
                        "Ya disparaste aquí";
                    default ->
                        "Resultado desconocido";
                };
                JOptionPane.showMessageDialog(this, mensaje);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            logger.severe("Error en onCeldaClick: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void actualizarInterfaz() {
        if (lblEstadoTurno != null) {
            if (partidaController.esTurnoDelJugador()) {
                lblEstadoTurno.setText("✅ TU TURNO - ¡DISPARA!");
                lblEstadoTurno.setForeground(java.awt.Color.GREEN);
            } else {
                lblEstadoTurno.setText("⏳ TURNO DEL OPONENTE");
                lblEstadoTurno.setForeground(java.awt.Color.RED);
            }
        }

        if (lblEstadoPartida != null) {
            if (partidaController.esPartidaActiva()) {
                lblEstadoPartida.setText("PARTIDA ACTIVA");
                lblEstadoPartida.setForeground(java.awt.Color.BLUE);
            } else {
                lblEstadoPartida.setText("PARTIDA TERMINADA");
                lblEstadoPartida.setForeground(java.awt.Color.ORANGE);
            }
        }
    }

    // ========== IMPLEMENTACIÓN DE VistaTableroListener ==========
    @Override
    public void onTableroActualizado(Tablero tablero) {
        System.out.println("🎨 PanelJuego.onTableroActualizado() - Actualizando botones");

        javax.swing.SwingUtilities.invokeLater(() -> {
            // ✅ ESTO ES LO QUE FALTABA: Actualizar los botones
            controlVista.actualizarBotones(tablero);
            actualizarInterfaz();  // También actualizar estado del turno
        });
    }

    // ========== IMPLEMENTACIÓN DE ViewUpdateListener ==========
    @Override
    public void onTurnoActualizado(boolean esMiTurno) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (lblEstadoTurno != null) {
                if (esMiTurno) {
                    lblEstadoTurno.setText("✅ TU TURNO - ¡DISPARA!");
                    lblEstadoTurno.setForeground(java.awt.Color.GREEN);
                } else {
                    lblEstadoTurno.setText("⏳ TURNO DEL OPONENTE");
                    lblEstadoTurno.setForeground(java.awt.Color.RED);
                }
            }
        });
    }

    @Override
    public void onPartidaEstadoActualizado(battleship.domain.enums.EstadoPartida estado, String ganador) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (lblEstadoPartida != null) {
                switch (estado) {
                    case ACTIVA:
                        lblEstadoPartida.setText("PARTIDA ACTIVA");
                        lblEstadoPartida.setForeground(java.awt.Color.BLUE);
                        break;
                    case FINALIZADA:
                        lblEstadoPartida.setText("GANADOR: " + ganador);
                        lblEstadoPartida.setForeground(java.awt.Color.GREEN);

                        JOptionPane.showMessageDialog(this,
                                "🎉 ¡" + ganador + " es el ganador!",
                                "Partida Terminada",
                                JOptionPane.INFORMATION_MESSAGE);
                        break;
                    default:
                        lblEstadoPartida.setText(estado.toString());
                        lblEstadoPartida.setForeground(java.awt.Color.GRAY);
                }
            }
        });
    }

    @Override
    public void onErrorOcurrido(String mensaje) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void onMensajeRecibido(String mensaje) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            System.out.println("📢 Mensaje del Controller: " + mensaje);
        });
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelTablero = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanelTableroOponente = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1400, 800));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelTablero.setOpaque(false);
        panelTablero.setLayout(null);
        getContentPane().add(panelTablero, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 100, 600, 600));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/PantallaJuego.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        getContentPane().add(jPanelTableroOponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 100, 600, 600));

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelTableroOponente;
    private javax.swing.JPanel panelTablero;
    // End of variables declaration//GEN-END:variables

}
