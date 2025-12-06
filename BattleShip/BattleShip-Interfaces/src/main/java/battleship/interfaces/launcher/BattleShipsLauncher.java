package battleship.interfaces.launcher;

import battleship.application.ControlJuego;
import battleship.infrastructure.network.P2PManager;
import battleship.interfaces.controllers.ControlVista;
import battleship.interfaces.views.Menu;
import javax.swing.*;

public class BattleShipsLauncher {
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        System.out.println("🎮 INICIANDO BATTLESHIP P2P");
        System.out.println("============================");
        
        // Abrir directamente el menú principal del juego
        SwingUtilities.invokeLater(() -> {
            abrirMenuPrincipal();
        });
    }
    
    private static void abrirMenuPrincipal() {
        try {
            System.out.println("🚀 Iniciando BATTLESHIP...");
            
            P2PManager p2pService = new P2PManager();
            ControlJuego controlJuego = new ControlJuego(p2pService);
            ControlVista controlVista = new ControlVista(controlJuego);
            
            Menu menu = new Menu(controlVista);
            menu.setTitle("🚢 BATTLESHIP - Menú Principal");
            menu.setSize(1400, 800);
            menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            menu.setResizable(false);
            menu.setLocationRelativeTo(null);
            menu.setVisible(true);
            
            System.out.println("✅ Menú principal listo");
            
            // Mostrar instrucciones básicas
            mostrarInstruccionesBasicas(menu);
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar el juego: " + e.getMessage());
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(null,
                "Error al iniciar el juego:\n" + e.getMessage() + "\n\n" +
                "Verifica que todos los componentes estén correctamente configurados.",
                "Error de Inicio",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void mostrarInstruccionesBasicas(JFrame ventana) {
        Timer timer = new Timer(1000, e -> {
            String mensaje = "<html><div style='text-align: center;'>"
                    + "<h2>🚢 BIENVENIDO A BATTLESHIP</h2><br>"
                    + "<b>Dos formas de jugar:</b><br><br>"
                    + "🎮 <b>CREAR PARTIDA</b> (Serás el SERVIDOR)<br>"
                    + "<i>Crea una nueva partida y comparte el ID</i><br><br>"
                    + "🎯 <b>UNIRSE A PARTIDA</b> (Serás el CLIENTE)<br>"
                    + "<i>Únete a una partida usando el ID del servidor</i><br><br>"
                    + "⚠️ <font size='2'>Ambos jugadores necesitan estar en la misma red</font>"
                    + "</div></html>";
            
            JOptionPane optionPane = new JOptionPane(
                mensaje,
                JOptionPane.INFORMATION_MESSAGE
            );
            
            JDialog dialog = optionPane.createDialog(ventana, "Instrucciones");
            dialog.setModal(true);
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(ventana);
            dialog.setVisible(true);
        });
        timer.setRepeats(false);
        timer.start();
    }
   
}