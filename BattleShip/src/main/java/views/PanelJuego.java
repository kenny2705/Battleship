/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import controllers.ControlJuego;
import controllers.ControlVista;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import models.Jugador;
import models.Tablero;
import models.TableroObservador;

/**
 *
 * @author Acer
 */
public class PanelJuego extends javax.swing.JFrame implements TableroObservador {

    private Jugador jugador;
    private ControlVista controlVista;
    private ControlJuego controlJuego;

    private JPanel panelPropio;
    private JPanel panelOponente;

    private JLabel lblTemporizador;
    private JLabel lblEstadoTurno;

    private static final int CASILLA_SIZE = 45;
    private static final int MARGEN_COORD = 30; 
    private static final int TABLERO_SIZE = CASILLA_SIZE * 10; 

    public PanelJuego(Jugador jugador, ControlJuego controlJuego, ControlVista controlVista) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Batalla Naval - " + (jugador != null ? jugador.getNombre() : ""));
        setSize(1350, 800);
        setResizable(false);
        setLayout(null); 

        getContentPane().setBackground(new Color(40, 44, 52));

        this.jugador = jugador;
        this.controlJuego = controlJuego;
        this.controlVista = controlVista;

        inicializarComponentes();
        inicializarTableros();

        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        //TEMPORIZADOR 
        lblTemporizador = new JLabel("30", SwingConstants.CENTER);
        lblTemporizador.setFont(new Font("Arial", Font.BOLD, 48));
        lblTemporizador.setForeground(Color.YELLOW);
        lblTemporizador.setBounds(580, 20, 140, 60);
        add(lblTemporizador);

        //INDICADOR DE TURNO
        lblEstadoTurno = new JLabel("ESPERANDO INICIO...", SwingConstants.CENTER);
        lblEstadoTurno.setFont(new Font("Arial", Font.BOLD, 24));
        lblEstadoTurno.setForeground(Color.WHITE);
        lblEstadoTurno.setBounds(400, 80, 500, 30);
        add(lblEstadoTurno);

        //TABLEROS CON COORDENADAS
        int xPropio = 50;
        int xRival = 800;
        int yTableros = 150;

        agregarEtiqueta(xPropio, 110, TABLERO_SIZE + MARGEN_COORD, "MI FLOTA");
        agregarEtiqueta(xRival, 110, TABLERO_SIZE + MARGEN_COORD, "RADAR ENEMIGO");
        //TABLERO PROPIO
        agregarCoordenadas(xPropio, yTableros);

        panelPropio = new JPanel(new GridLayout(10, 10));
        panelPropio.setBounds(xPropio + MARGEN_COORD, yTableros + MARGEN_COORD, TABLERO_SIZE, TABLERO_SIZE);
        panelPropio.setBackground(Color.BLACK);
        add(panelPropio);
        //TABLERO RIVAL
        agregarCoordenadas(xRival, yTableros);

        panelOponente = new JPanel(new GridLayout(10, 10));
        panelOponente.setBounds(xRival + MARGEN_COORD, yTableros + MARGEN_COORD, TABLERO_SIZE, TABLERO_SIZE);
        panelOponente.setBackground(Color.BLACK);
        add(panelOponente);
    }

    // AGREGA LAS COORDENADAS EN FORMATO (A-J  1-10)
    private void agregarCoordenadas(int x, int y) {
        Font fontCoord = new Font("Arial", Font.BOLD, 14);
        Color colorCoord = Color.LIGHT_GRAY;

        // COLUMNAS (1-10) PARTE SUPERIOR
        for (int i = 0; i < 10; i++) {
            JLabel lbl = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            lbl.setFont(fontCoord);
            lbl.setForeground(colorCoord);
            lbl.setBounds(x + MARGEN_COORD + (i * CASILLA_SIZE), y, CASILLA_SIZE, MARGEN_COORD);
            add(lbl);
        }

        // FILAS (A-J) LATERAL IZQUIERDO
        for (int i = 0; i < 10; i++) {
            char letra = (char) ('A' + i);
            JLabel lbl = new JLabel(String.valueOf(letra), SwingConstants.CENTER);
            lbl.setFont(fontCoord);
            lbl.setForeground(colorCoord);
            lbl.setBounds(x, y + MARGEN_COORD + (i * CASILLA_SIZE), MARGEN_COORD, CASILLA_SIZE);
            add(lbl);
        }
    }

    private void agregarEtiqueta(int x, int y, int w, String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, 18));
        l.setForeground(Color.WHITE);
        l.setBounds(x, y, w, 30);
        add(l);
    }

    // NUMERO DEL TEMPORIZADOR
    public void actualizarTemporizador(int segundos) {
        lblTemporizador.setText(String.valueOf(segundos));
        if (segundos <= 5) {
            lblTemporizador.setForeground(Color.RED);
        } else {
            lblTemporizador.setForeground(Color.YELLOW);
        }
    }

    // TEXTO DEL TURNO (TU TURNO / RIVAL)
    public void actualizarEstado(String estado) {
        lblEstadoTurno.setText(estado);
        if (estado.contains("TU TURNO")) {
            lblEstadoTurno.setForeground(Color.GREEN);
        } else {
            lblEstadoTurno.setForeground(Color.WHITE);
        }
    }

    private void inicializarTableros() {
        if (jugador != null && controlVista != null) {
            // Inicializar Tablero Propio
            Tablero propio = jugador.getTablero();
            if (propio.getMatrizDeCasillas().isEmpty()) {
                propio.inicializarCasillas();
            }
            propio.addObservador(this);
            controlVista.generarTableroPropio(propio, panelPropio, 45);

            // Inicializar Tablero Rival
            Tablero rival = controlJuego.getOponenteTablero();
            if (rival.getMatrizDeCasillas().isEmpty()) {
                rival.inicializarCasillas();
            }
            rival.addObservador(this);
            controlVista.generarTableroOponente(rival, panelOponente, 45);
        }
    }


    public PanelJuego() {
        initComponents();
    }
 public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new PanelJuego(null, null, null).setVisible(true);
        });
    }

    @Override
    public void actualizarTablero(Tablero tablero, String mensaje) {
        if (controlVista != null) {
            if (tablero == jugador.getTablero()) {
                controlVista.actualizarTableroPropio(tablero);
            } else {
                controlVista.actualizarTableroOponente(tablero);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // </editor-fold>
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
