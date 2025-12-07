/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.battleship;

import controllers.ControlJuego;
import controllers.ControlVista;
import views.Menu;

/**
 *
 * @author Usuario
 */
public class Battleship {

    public static void main(String[] args) {
        // 1. Crear el Controlador de Juego (Modelo y Lógica)
        ControlJuego controlJuego = new ControlJuego();

        // 2. Crear el Controlador de Vista (Puente entre Lógica y UI/P2P)
        ControlVista controlVista = new ControlVista(controlJuego);

        // 3. Iniciar la interfaz de usuario (Ventana principal)
        java.awt.EventQueue.invokeLater(() -> {
            // Pasamos el controlador de vista a la vista de menú.
            new Menu(controlVista).setVisible(true);
        });
    }
}
