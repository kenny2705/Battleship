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
public class Tablero {
    private int medidas;
    private List<Casilla> matrizDeCasillas;
    private Jugador jugador;
    private List<Nave> naves;
    private List<Disparo> disparos;

    //Observadores 
    public Tablero() {
    }

    public Tablero(int medidas, List<Casilla> matrizDeCasillas, Jugador jugador, List<Nave> naves, List<Disparo> disparos) {
        this.medidas = medidas;
        this.matrizDeCasillas = matrizDeCasillas;
        this.jugador = jugador;
        this.naves = naves;
        this.disparos = disparos;
    }

    public int getMedidas() {
        return medidas;
    }

    public void setMedidas(int medidas) {
        this.medidas = medidas;
    }

    public List<Casilla> getMatrizDeCasillas() {
        return matrizDeCasillas;
    }

    public void setMatrizDeCasillas(List<Casilla> matrizDeCasillas) {
        this.matrizDeCasillas = matrizDeCasillas;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public List<Nave> getNaves() {
        return naves;
    }

    public void setNaves(List<Nave> naves) {
        this.naves = naves;
    }

    public List<Disparo> getDisparos() {
        return disparos;
    }

    public void setDisparos(List<Disparo> disparos) {
        this.disparos = disparos;
    }
    
    public void colocarNavesEnCasillas() {
    if (naves == null || naves.isEmpty()) {
        return;
    }

        for (Nave nave : naves) {
            for (String coord : nave.getCoordenadas()) {
                for (Casilla casilla : matrizDeCasillas) {
                    if (casilla.getCoordenada().equals(coord)) {
                        casilla.setOcupada(true);
                        break;
                    }
                }
            }
        }
    }
    
    
}
