/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import controllers.ControlJuego;
import controllers.ControlVista;
import java.awt.Color;
import java.awt.Font;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
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

    private static final Logger logger = Logger.getLogger(PanelJuego.class.getName());

    // Variables de lógica
    private Jugador jugador;
    private ControlVista controlVista;
    private ControlJuego controlJuego;

    // Componentes Gráficos
    private JPanel panelPropio;   // Tablero izquierdo: Mis naves y ataques recibidos
    private JPanel panelOponente; // Tablero derecho: Mis ataques al rival
    private JLabel jLabelFondo;
    private JLabel lblTituloPropio;
    private JLabel lblTituloOponente;

    // Constantes para el diseño
    private static final int TAMANO_CASILLA = 40; // Tamaño de cada botón
    private static final int MARGEN_TABLERO = 30; // Espacio para coordenadas
    private static final int ESPACIO_ENTRE_TABLEROS = 50;
    private static final int INICIO_X_PROPIO = 70;
    private static final int INICIO_Y_TABLEROS = 120;
    private static final int ANCHO_TABLERO_PANEL = TAMANO_CASILLA * 10;
    private static final int ALTO_TABLERO_PANEL = TAMANO_CASILLA * 10;
    private static final int INICIO_X_OPONENTE = INICIO_X_PROPIO + ANCHO_TABLERO_PANEL + MARGEN_TABLERO + ESPACIO_ENTRE_TABLEROS;

    public PanelJuego(Jugador jugador, ControlJuego controlJuego, ControlVista controlVista) {
        // 1. Configuración de la Ventana
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Batalla Naval - En Juego (" + (jugador != null ? jugador.getNombre() : "Jugador") + ")");
        setSize(1200, 750); // Ventana más ancha para dos tableros y más alta para coordenadas
        setMinimumSize(new java.awt.Dimension(1200, 750));
        setResizable(false);
        setLayout(null); // Layout absoluto para mantener el diseño personalizado

        this.jugador = jugador;
        this.controlJuego = controlJuego;
        this.controlVista = controlVista;

        // 2. Inicialización de Componentes Gráficos
        // --- Títulos ---
        lblTituloPropio = new JLabel("MI FLOTA", SwingConstants.CENTER);
        lblTituloPropio.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloPropio.setForeground(Color.WHITE);
        lblTituloPropio.setBounds(INICIO_X_PROPIO, INICIO_Y_TABLEROS - 40, ANCHO_TABLERO_PANEL + MARGEN_TABLERO, 30);
        add(lblTituloPropio);

        lblTituloOponente = new JLabel("RADAR ENEMIGO", SwingConstants.CENTER);
        lblTituloOponente.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloOponente.setForeground(Color.WHITE);
        lblTituloOponente.setBounds(INICIO_X_OPONENTE, INICIO_Y_TABLEROS - 40, ANCHO_TABLERO_PANEL + MARGEN_TABLERO, 30);
        add(lblTituloOponente);

        // --- Tablero Propio (Izquierda) ---
        panelPropio = new JPanel();
        panelPropio.setOpaque(false);
        // El panel de botones se desplaza por el margen de las coordenadas
        panelPropio.setBounds(INICIO_X_PROPIO + MARGEN_TABLERO, INICIO_Y_TABLEROS + MARGEN_TABLERO, ANCHO_TABLERO_PANEL, ALTO_TABLERO_PANEL);
        panelPropio.setLayout(null);
        add(panelPropio);
        agregarCoordenadas(INICIO_X_PROPIO, INICIO_Y_TABLEROS);

        // --- Tablero Oponente (Derecha) ---
        panelOponente = new JPanel();
        panelOponente.setOpaque(false);
        // El panel de botones se desplaza por el margen de las coordenadas
        panelOponente.setBounds(INICIO_X_OPONENTE + MARGEN_TABLERO, INICIO_Y_TABLEROS + MARGEN_TABLERO, ANCHO_TABLERO_PANEL, ALTO_TABLERO_PANEL);
        panelOponente.setLayout(null);
        add(panelOponente);
        agregarCoordenadas(INICIO_X_OPONENTE, INICIO_Y_TABLEROS);

        // --- Fondo ---
        jLabelFondo = new JLabel();
        jLabelFondo.setBounds(0, 0, 1200, 750);
        cargarImagenFondo();
        add(jLabelFondo); // Agregar fondo al final para que quede atrás de todo

        // 3. Lógica de Inicialización de Tableros
        inicializarTableros();

        setLocationRelativeTo(null); // Centrar ventana
    }

    private void cargarImagenFondo() {
        try {
            // Usamos PanelJuego.class para obtener el recurso de forma segura
            java.net.URL imgUrl = PanelJuego.class.getResource("/imagenes/PantallaJuego.png");
            if (imgUrl != null) {
                jLabelFondo.setIcon(new ImageIcon(imgUrl));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de fondo: " + e.getMessage());
            // Fondo de respaldo si falla la imagen
            getContentPane().setBackground(new Color(0, 102, 153));
        }
    }

    private void agregarCoordenadas(int inicioX, int inicioY) {
        Font fontCoordenadas = new Font("Arial", Font.BOLD, 14);
        Color colorCoordenadas = Color.WHITE;

        // Filas (A-J) en el margen izquierdo
        for (int i = 0; i < 10; i++) {
            JLabel lblFila = new JLabel(String.valueOf((char) ('A' + i)), SwingConstants.CENTER);
            lblFila.setFont(fontCoordenadas);
            lblFila.setForeground(colorCoordenadas);
            lblFila.setBounds(inicioX, inicioY + MARGEN_TABLERO + (i * TAMANO_CASILLA), MARGEN_TABLERO, TAMANO_CASILLA);
            add(lblFila);
        }

        // Columnas (1-10) en el margen superior
        for (int i = 0; i < 10; i++) {
            JLabel lblCol = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            lblCol.setFont(fontCoordenadas);
            lblCol.setForeground(colorCoordenadas);
            lblCol.setBounds(inicioX + MARGEN_TABLERO + (i * TAMANO_CASILLA), inicioY, TAMANO_CASILLA, MARGEN_TABLERO);
            add(lblCol);
        }
    }

    private void inicializarTableros() {
        if (jugador == null || controlJuego == null || controlVista == null) {
            return;
        }

        // --- Inicializar Tablero Propio ---
        Tablero tableroPropio = jugador.getTablero();
        if (tableroPropio != null) {
            if (tableroPropio.getMatrizDeCasillas().isEmpty()) {
                tableroPropio.inicializarCasillas();
            }
            tableroPropio.addObservador(this);
            // Llama a un método específico en ControlVista para generar el tablero propio (visualización de naves)
            // NOTA: Este método debe ser implementado en ControlVista.
            controlVista.generarTableroPropio(tableroPropio, panelPropio, TAMANO_CASILLA);
        }

        // --- Inicializar Tablero Oponente ---
        Tablero tableroOponente = controlJuego.getOponenteTablero();
        if (tableroOponente != null) {
            if (tableroOponente.getMatrizDeCasillas().isEmpty()) {
                tableroOponente.inicializarCasillas();
            }
            tableroOponente.addObservador(this);
            // Llama a un método específico en ControlVista para generar el tablero del oponente (para atacar)
            // NOTA: Este método debe ser implementado en ControlVista.
            controlVista.generarTableroOponente(tableroOponente, panelOponente, TAMANO_CASILLA);
        }
    }

    // Constructor vacío (si lo necesitas para pruebas de diseño)
    public PanelJuego() {
        this(null, null, null);
    }

    public static void main(String args[]) {
        // Método main para pruebas aisladas.
        // Requeriría una configuración de dependencias (Jugador, ControlJuego, ControlVista)
        // adecuada para funcionar con la nueva estructura de dos tableros.
        java.awt.EventQueue.invokeLater(() -> {
            // Ejemplo básico (no funcionará completamente sin la lógica de ControlVista):
            // ControlJuego cj = new ControlJuego();
            // ControlVista cv = new ControlVista(cj);
            // new PanelJuego(cj.getJugador(), cj, cv).setVisible(true);
        });
    }

    @Override
    public void actualizarTablero(Tablero tablero, String mensaje) {
        if (controlVista == null || jugador == null || controlJuego == null) {
            return;
        }

        // Determinar qué tablero ha cambiado y llamar al método de actualización correspondiente en ControlVista.
        // NOTA: Los métodos actualizarTableroPropio y actualizarTableroOponente deben ser implementados en ControlVista.
        if (tablero == jugador.getTablero()) {
            // Se actualizó el tablero propio (ej. recibí un disparo)
            controlVista.actualizarTableroPropio(tablero);
        } else if (tablero == controlJuego.getOponenteTablero()) {
            // Se actualizó el tablero del oponente (ej. realicé un disparo y obtuve resultado)
            controlVista.actualizarTableroOponente(tablero);
        }

        System.out.println("Notificación recibida en PanelJuego (" + jugador.getNombre() + "): " + mensaje);
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
