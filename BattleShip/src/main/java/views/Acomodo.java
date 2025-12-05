/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import controllers.ControlJuego;
import controllers.ControlVista;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import models.Casilla;
import models.Jugador;
import models.Nave;
import models.Tablero;

/**
 *
 * @author Acer
 */
public class Acomodo extends javax.swing.JFrame {
    private ControlJuego controlJuego;
    private ControlVista controlVista;
    private Jugador jugador;

    // Componentes UI
    private JPanel panelTablero;
    private JPanel panelNaves; // Nuevo panel para mostrar y seleccionar naves
    private JButton btnListo;
    private JButton btnOrientacion; // Botón para cambiar la orientación
    private final Map<String, JButton> botonesCasillas = new HashMap<>();

    // Estado local para la Interacción de la Vista (Bajo acoplamiento)
    private Nave naveSeleccionada = null;
    private int orientacion = 0; // 0=Horizontal, 1=Vertical

    // Constructor completo (usado por ControlVista)
    public Acomodo(ControlJuego controlJuego, ControlVista controlVista) {
        this.controlJuego = controlJuego;
        this.controlVista = controlVista;
        this.jugador = controlJuego.getJugador();

        initComponents();
        inicializarComponentesPersonalizados();
        cargarTableroInicial();
        cargarNavesDisponibles();
        btnListo.setEnabled(false); // Deshabilitar hasta que todas las naves estén colocadas
    }

    // Constructor sin parámetros (mantener para el IDE)
    public Acomodo() {
        initComponents();
        // Solo para pruebas en el main, inicializar con mock data si es necesario
    }

    // --- LÓGICA DE LA VISTA (Solo Interacción y Presentación) ---
    private void inicializarComponentesPersonalizados() {
        setTitle("Acomodo de Naves - Jugador: " + jugador.getId());

        // Contenedores principales
        getContentPane().setLayout(new java.awt.BorderLayout(10, 10)); // Espaciado

        // Panel Central para el Tablero
        panelTablero = new JPanel();
        panelTablero.setLayout(new GridLayout(jugador.getTableros().get(0).getMedidas(), jugador.getTableros().get(0).getMedidas()));
        panelTablero.setPreferredSize(new Dimension(500, 500));

        // Panel Derecho para Controles (Naves y Botones)
        JPanel panelControles = new JPanel();
        panelControles.setLayout(new javax.swing.BoxLayout(panelControles, javax.swing.BoxLayout.Y_AXIS));
        panelControles.setPreferredSize(new Dimension(200, 500));

        // Panel para Naves
        panelNaves = new JPanel();
        panelNaves.setLayout(new GridLayout(0, 1, 5, 5)); // Una columna, espaciado de 5px
        panelControles.add(new javax.swing.JLabel("Naves Disponibles:"));
        panelControles.add(panelNaves);

        // Botón de Orientación
        btnOrientacion = new JButton("Orientación: Horizontal");
        btnOrientacion.addActionListener(e -> cambiarOrientacion());
        panelControles.add(btnOrientacion);

        // Botón Listo
        btnListo = new JButton("Listo para Batalla");
        btnListo.addActionListener(this::btnListoActionPerformed);
        panelControles.add(btnListo);

        // Añadir componentes al JFrame
        getContentPane().add(panelTablero, java.awt.BorderLayout.CENTER);
        getContentPane().add(panelControles, java.awt.BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    private void cambiarOrientacion() {
        orientacion = 1 - orientacion; // Cambia entre 0 (Horizontal) y 1 (Vertical)
        String texto = orientacion == 0 ? "Horizontal" : "Vertical";
        btnOrientacion.setText("Orientación: " + texto);

        // Opcional: Recalcar la nave seleccionada
        if (naveSeleccionada != null) {
            JOptionPane.showMessageDialog(this, "Orientación cambiada a: " + texto + " para la nave " + naveSeleccionada.getTipo(), "Orientación", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cargarTableroInicial() {
        Tablero tablero = jugador.getTableros().get(0);
        int n = tablero.getMedidas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coordenada = convertirCoordenada(fila, col);
                JButton btn = new JButton(coordenada);
                btn.setPreferredSize(new Dimension(50, 50));
                btn.setBackground(java.awt.Color.CYAN); // Color de agua inicial
                btn.setOpaque(true);
                btn.setBorderPainted(false);

                // Evento de clic en la casilla para colocar naves
                btn.addActionListener(e -> {
                    // La vista solo informa la intención de colocar en (coordenada)
                    intentarColocarNave(coordenada);
                });

                panelTablero.add(btn);
                botonesCasillas.put(coordenada, btn);
            }
        }
    }

    private void cargarNavesDisponibles() {
        panelNaves.removeAll();
        List<Nave> navesDisponibles = jugador.getNaves();

        if (navesDisponibles != null) {
            for (Nave nave : navesDisponibles) {
                if (!nave.isColocada()) { // Solo muestra las no colocadas
                    JButton btnNave = new JButton(nave.getTipo() + " (" + nave.getTamanio() + ")");
                    btnNave.addActionListener(e -> seleccionarNave(nave, btnNave));
                    panelNaves.add(btnNave);
                }
            }
        }

        // Selecciona automáticamente la primera si no hay ninguna seleccionada
        if (naveSeleccionada == null && navesDisponibles != null && !navesDisponibles.isEmpty()) {
            seleccionarNave(navesDisponibles.stream().filter(n -> !n.isColocada()).findFirst().orElse(null), null);
        }

        panelNaves.revalidate();
        panelNaves.repaint();
    }

    // Método para manejar la selección de una nave
    private void seleccionarNave(Nave nave, JButton clickedButton) {
        if (nave == null || nave.isColocada()) {
            return;
        }

        // Resetea el color del botón previamente seleccionado
        for (java.awt.Component comp : panelNaves.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBackground(null);
            }
        }

        naveSeleccionada = nave;
        if (clickedButton != null) {
            clickedButton.setBackground(java.awt.Color.YELLOW); // Resalta la nave seleccionada
            clickedButton.setOpaque(true);
        }

        JOptionPane.showMessageDialog(this,
                "Nave seleccionada: " + naveSeleccionada.getTipo() + ". Haz clic en el tablero para colocarla.",
                "Selección",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Simula la colocación de una nave al hacer clic en una casilla
    private void intentarColocarNave(String coordenadaInicio) {
        if (naveSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una nave primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Tablero tablero = jugador.getTableros().get(0);

        // **ALTA COHESIÓN**: La validación y colocación es responsabilidad del ControlJuego.
        // La vista solo pasa los datos de la interacción.
        boolean colocada = controlJuego.colocarNave(tablero, naveSeleccionada, coordenadaInicio, orientacion);

        if (colocada) {
            // Éxito: Actualizar UI y pasar a la siguiente nave
            actualizarVistaTablero(tablero);

            // Recargar panel de naves (la nave colocada desaparecerá)
            cargarNavesDisponibles();

            // Verificar si el acomodo está completo
            if (controlJuego.verificarAcomodoCompleto(jugador)) {
                JOptionPane.showMessageDialog(this, "Todas las naves han sido colocadas. ¡Listo para la batalla!", "Acomodo Completo", JOptionPane.INFORMATION_MESSAGE);
                btnListo.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Coloca la siguiente nave.", "Siguiente", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se puede colocar la nave aquí (fuera de límites, superposición, o mala posición).",
                    "Error de Acomodo",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper para obtener la siguiente nave pendiente de colocar (Ya no es necesario, ControlJuego lo verificará)
    // private Nave getSiguienteNaveNoColocada(List<Nave> naves) { ... } // ELIMINADO
    private void actualizarVistaTablero(Tablero tablero) {
        // Método para reflejar los cambios en el modelo (las naves colocadas) en el UI
        for (Casilla casilla : tablero.getMatrizDeCasillas()) {
            JButton btn = botonesCasillas.get(casilla.getCoordenada());
            if (btn != null) {
                if (casilla.isOcupada()) {
                    btn.setBackground(java.awt.Color.GRAY); // Representación visual de la nave
                    btn.setText("");
                } else {
                    btn.setBackground(java.awt.Color.CYAN); // Agua
                    btn.setText(casilla.getCoordenada());
                }
                btn.setOpaque(true);
                btn.setBorderPainted(false);
            }
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    private String convertirCoordenada(int fila, int col) {
                char letra = (char) ('A' + fila);
                return letra + String.valueOf(col + 1);
    

        }

    // Manejo del evento "Listo"
    private void btnListoActionPerformed(java.awt.event.ActionEvent evt) {

        // La validación final se hace en ControlJuego
        boolean acomodoCompleto = controlJuego.verificarAcomodoCompleto(jugador);

        if (acomodoCompleto) {
            // Se llama al ControlVista para que inicie el proceso de sincronización P2P
            // La vista se encarga de cerrarse a sí misma.
            controlVista.notificarAcomodoListo(jugador); // Se debe pasar el Jugador al controlador
            this.dispose();
        } else {
            // Este caso ya debería ser manejado por la lógica de habilitar/deshabilitar el botón
            JOptionPane.showMessageDialog(this,
                    "Debes colocar todas tus naves antes de continuar.",
                    "Acomodo Incompleto",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Acomodo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Acomodo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Acomodo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Acomodo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Acomodo().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
