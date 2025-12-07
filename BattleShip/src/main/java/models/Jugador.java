/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class Jugador {

    private String nombre;
    private String color;
    private boolean turno;

    private Marcador marcador;

    private Tablero tablero;

    public Jugador() {
        this.marcador = new Marcador();
    }

    public Jugador(String nombre, String color, boolean turno, Marcador marcador, Tablero tablero) {
        this.nombre = nombre;
        this.color = color;
        this.turno = turno;
        this.marcador = marcador != null ? marcador : new Marcador();
        this.tablero = tablero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isTurno() {
        return turno;
    }

    public void setTurno(boolean turno) {
        this.turno = turno;
    }

    public Marcador getMarcador() {
        return marcador;
    }

    public void setMarcador(Marcador marcador) {
        this.marcador = marcador;
    }


    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public List<Nave> getNaves() {
        if (tablero == null) {
            return new ArrayList<>();
        }
        return tablero.getNaves();
    }
    public String getId() {
        return nombre != null ? nombre : "JugadorSinNombre";
    }
    
    public boolean isAcomodoCompleto() {
        if (tablero == null || tablero.getNaves() == null) {
            return false;
        }
        // Itera sobre las naves para ver si alguna no ha sido colocada.
        for (Nave nave : tablero.getNaves()) {
            if (!nave.isColocada()) {
                return false;
            }
        }
        return true;
    }
}
