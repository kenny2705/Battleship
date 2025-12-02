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

        ControlJuego controlJuego = new ControlJuego();
        ControlVista controlVista = new ControlVista(controlJuego);

        Menu menu = new Menu(controlVista);
        controlVista.setMenuView(menu);

        menu.setVisible(true);
    }
}

