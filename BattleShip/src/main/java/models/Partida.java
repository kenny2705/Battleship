/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import models.enums.EstadoPartida;

/**
 *
 * @author Usuario
 */
public class Partida {
    EstadoPartida estadoPartida;
    private Jugador ganador;
    int turno;

    public Partida() {
    }

    public Partida(EstadoPartida estadoPartida, Jugador ganador, int turno) {
        this.estadoPartida = estadoPartida;
        this.ganador = ganador;
        this.turno = turno;
    }

    public EstadoPartida getEstadoPartida() {
        return estadoPartida;
    }

    public void setEstadoPartida(EstadoPartida estadoPartida) {
        this.estadoPartida = estadoPartida;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public void setGanador(Jugador ganador) {
        this.ganador = ganador;
    }

    public int getTurno() {
        return turno;
    }

    public void setTurno(int turno) {
        this.turno = turno;
    }
    
    
}
