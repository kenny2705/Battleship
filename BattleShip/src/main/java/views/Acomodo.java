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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

    private JPanel panelTablero;
    private JPanel panelNaves;
    private JButton btnListo;
    private JButton btnOrientacion;
    private final Map<String, JButton> botonesCasillas = new HashMap<String, JButton>();

    private Nave naveSeleccionada = null;
    private int orientacion = 0;

    public Acomodo(Jugador jugador, ControlJuego controlJuego, ControlVista controlVista) {
        this.jugador = jugador;
        this.controlJuego = controlJuego;
        this.controlVista = controlVista;

        initComponents();
        inicializarComponentesPersonalizados();
        cargarTableroInicial();
        cargarNavesDisponibles();
    }

    public Acomodo() {
        initComponents();
    }

    private void inicializarComponentesPersonalizados() {
        if (jugador != null) {
            setTitle("Acomodo de Naves - " + jugador.getNombre());
        } else {
            setTitle("Acomodo de Naves");
        }

        getContentPane().setLayout(new java.awt.BorderLayout(10, 10));

        Tablero tablero = jugador != null ? jugador.getTablero() : new Tablero(10, null);
        int n = tablero.getMedidas();

        panelTablero = new JPanel();
        panelTablero.setLayout(new GridLayout(n, n));
        panelTablero.setPreferredSize(new Dimension(550, 550)); 

        JPanel panelControles = new JPanel();
        panelControles.setLayout(new javax.swing.BoxLayout(panelControles, javax.swing.BoxLayout.Y_AXIS));
        panelControles.setPreferredSize(new Dimension(220, 500));

        panelNaves = new JPanel();
        panelNaves.setLayout(new GridLayout(0, 1, 5, 5)); 
        panelControles.add(new javax.swing.JLabel("Naves Disponibles:"));
        panelControles.add(panelNaves);

        btnOrientacion = new JButton("Orientacion: Horizontal");
        btnOrientacion.addActionListener(e -> cambiarOrientacion());
        panelControles.add(btnOrientacion);


        btnListo = new JButton("Listo para Batalla");
        btnListo.addActionListener(this::btnListoActionPerformed);
        btnListo.setEnabled(false); // Se habilita solo al completar
        panelControles.add(btnListo);


        getContentPane().add(panelTablero, java.awt.BorderLayout.CENTER);
        getContentPane().add(panelControles, java.awt.BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    private void cambiarOrientacion() {
        orientacion = 1 - orientacion;
        String texto = orientacion == 0 ? "Horizontal" : "Vertical";
        btnOrientacion.setText("Orientacion: " + texto);

        if (naveSeleccionada != null) {
            JOptionPane.showMessageDialog(this, "Orientacion cambiada a: " + texto + " para la nave " + naveSeleccionada.getTipo(), "Orientacion", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cargarTableroInicial() {
        if (jugador == null || jugador.getTablero() == null) {
            return;
        }
        Tablero tablero = jugador.getTablero();
        int n = tablero.getMedidas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coordenada = convertirCoordenada(fila, col);
                JButton btn = new JButton(coordenada);
                btn.setPreferredSize(new Dimension(50, 50));

                btn.setMargin(new java.awt.Insets(0, 0, 0, 0)); 
                btn.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10)); 
 
                btn.setBackground(java.awt.Color.CYAN);
                btn.setOpaque(true);
                btn.setBorderPainted(true);

                btn.addActionListener(e -> {
                    intentarColocarNave(coordenada);
                });

                panelTablero.add(btn);
                botonesCasillas.put(coordenada, btn);
            }
        }
    }

    private void cargarNavesDisponibles() {
        panelNaves.removeAll();
        if (jugador == null) {
            return;
        }

        List<Nave> navesDisponibles = jugador.getNaves();

        if (navesDisponibles != null) {
            for (Nave nave : navesDisponibles) {
                if (!nave.isColocada()) {
                    JButton btnNave = new JButton(nave.getTipo() + " (" + nave.getTamanio() + ")");
                    btnNave.addActionListener(e -> seleccionarNave(nave, btnNave));
                    panelNaves.add(btnNave);
                }
            }
        }

        if (naveSeleccionada == null && navesDisponibles != null) {
            for (Nave n : navesDisponibles) {
                if (!n.isColocada()) {
                    seleccionarNave(n, null);
                    break;
                }
            }
        }

        panelNaves.revalidate();
        panelNaves.repaint();
    }

    private void seleccionarNave(Nave nave, JButton clickedButton) {
        if (nave == null || nave.isColocada()) {
            return;
        }

        for (java.awt.Component comp : panelNaves.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBackground(null);
            }
        }

        naveSeleccionada = nave;
        if (clickedButton != null) {
            clickedButton.setBackground(java.awt.Color.YELLOW);
            clickedButton.setOpaque(true);
        }

        JOptionPane.showMessageDialog(this,
                "Nave seleccionada: " + naveSeleccionada.getTipo() + " (Tam: " + nave.getTamanio() + ").\nHaz clic en el tablero para colocarla.",
                "Selección",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void intentarColocarNave(String coordenadaInicio) {
        if (naveSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una nave primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (jugador == null || jugador.getTablero() == null) {
            return;
        }

        Tablero tablero = jugador.getTablero();

        boolean colocada = controlJuego.colocarNave(tablero, naveSeleccionada, coordenadaInicio, orientacion);

        if (colocada) {
            actualizarVistaTablero(tablero);
            cargarNavesDisponibles();

            naveSeleccionada = null;

            if (controlJuego.verificarAcomodoCompleto(jugador)) {
                JOptionPane.showMessageDialog(this, "Todas las naves han sido colocadas. ¡Listo para la batalla!", "Acomodo Completo", JOptionPane.INFORMATION_MESSAGE);
                btnListo.setEnabled(true);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se puede colocar la nave aqui (fuera de limites o superposicion).",
                    "Error de Acomodo",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarVistaTablero(Tablero tablero) {
        if (tablero == null) {
            return;
        }
        for (Casilla casilla : tablero.getMatrizDeCasillas()) {
            JButton btn = botonesCasillas.get(casilla.getCoordenada());
            if (btn != null) {
                if (casilla.isOcupada()) {
                    btn.setBackground(java.awt.Color.GRAY);
                } else {
                    btn.setBackground(java.awt.Color.CYAN);
                }
                btn.setOpaque(true);
            }
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    private String convertirCoordenada(int fila, int col) {
        char letra = (char) ('A' + fila);
        return letra + String.valueOf(col + 1);
    }

    private void btnListoActionPerformed(java.awt.event.ActionEvent evt) {
        if (jugador == null) {
            return;
        }

        boolean acomodoCompleto = controlJuego.verificarAcomodoCompleto(jugador);

        if (acomodoCompleto) {
            controlVista.notificarAcomodoListo(jugador);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Debes colocar todas tus naves antes de continuar.",
                    "Acomodo Incompleto",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
