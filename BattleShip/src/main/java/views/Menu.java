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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Acer
 */
public class Menu extends javax.swing.JFrame {

    private final ControlVista controlVista;
    private JButton btnCrearPartida;
    private JButton btnUnirsePartida;
    private JButton btnSalir;
    private JLabel lblTitulo;
    private JPanel panelPrincipal;

    public Menu(ControlVista controlVista) {
        this.controlVista = controlVista;

        setTitle("Batalla Naval - Menu Principal");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridLayout(4, 1, 10, 10)); 
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen
        panelPrincipal.setBackground(new Color(50, 50, 50)); 

        lblTitulo = new JLabel("BATTLESSHIP P2P");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);


        btnCrearPartida = crearBotonEstilizado("Crear Partida (Host)");
        btnUnirsePartida = crearBotonEstilizado("Unirse a Partida (Cliente)");
        btnSalir = crearBotonEstilizado("Salir");

        btnCrearPartida.addActionListener(e -> accionCrearPartida());
        btnUnirsePartida.addActionListener(e -> accionUnirsePartida());
        btnSalir.addActionListener(e -> System.exit(0));

        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(btnCrearPartida);
        panelPrincipal.add(btnUnirsePartida);
        panelPrincipal.add(btnSalir);

        this.add(panelPrincipal);
    }

    private JButton crearBotonEstilizado(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setFocusPainted(false);
        return btn;
    }

    private void accionCrearPartida() {
        if (controlVista != null) {
            controlVista.crearPartida();
            this.dispose(); 
        }
    }

    private void accionUnirsePartida() {
        String serverId = JOptionPane.showInputDialog(this,
                "Introduce el ID del servidor:",
                "Conectar",
                JOptionPane.QUESTION_MESSAGE);

        if (serverId != null && !serverId.trim().isEmpty()) {
            if (controlVista != null) {
                controlVista.unirseAPartida(serverId.trim());
                this.dispose(); 
            }
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(() -> {

            ControlJuego controlJuego = new ControlJuego();

            ControlVista controlVista = new ControlVista(controlJuego);
            controlJuego.setControlVista(controlVista);

            Menu menu = new Menu(controlVista);

            menu.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
