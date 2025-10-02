/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.battleship;

import controllers.ControlJuego;
import controllers.ControlVista;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import models.Jugador;
import models.Nave;
import models.Tablero;
import models.enums.EstadoNave;
import models.enums.TipoNave;
import views.PanelJuego;

/**
 *
 * @author Usuario
 */
public class Battleship {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ControlJuego controlJuego = new ControlJuego();

            // Crea la ventana principal del juego pasando jugador y controlador
            PanelJuego panel = new PanelJuego(controlJuego.getJugador(), controlJuego);
            panel.setVisible(true);
        });
    }
}
