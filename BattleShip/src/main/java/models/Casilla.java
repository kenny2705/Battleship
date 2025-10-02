/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author Usuario
 */
public class Casilla {
    private String coordenada;
    private boolean ocupada;
    private boolean dañada;

    public Casilla() {
    }

    public Casilla(String coordenada, boolean ocupada, boolean dañada) {
        this.coordenada = coordenada;
        this.ocupada = ocupada;
        this.dañada = dañada;
    }

    public String getCoordenada() {
        return coordenada;
    }

    public void setCoordenada(String coordenada) {
        this.coordenada = coordenada;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public boolean isDañada() {
        return dañada;
    }

    public void setDañada(boolean dañada) {
        this.dañada = dañada;
    }
}
