/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.List;

/**
 *
 * @author Usuario
 */
public class Jugador {
    private String nombre;
    private String color;
    private boolean turno;
    Marcador marcador;
    List<Tablero> tableros; 

    public Jugador() {
    }

    public Jugador(String nombre, String color, boolean turno, Marcador marcador, List<Tablero> tableros) {
        this.nombre = nombre;
        this.color = color;
        this.turno = turno;
        this.marcador = marcador;
        this.tableros = tableros;
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

    public List<Tablero> getTableros() {
        return tableros;
    }

    public void setTableros(List<Tablero> tableros) {
        this.tableros = tableros;
    }
    
    
}
