/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.ArrayList;
import java.util.List;
import models.enums.EstadoNave;
import models.enums.TipoNave;

/**
 *
 * @author Usuario
 */
public class Nave {

    private TipoNave tipoNave;
    private List<String> coordenadas;
    private int impactos;
    private EstadoNave estadoNave;
    private boolean colocada = false;

    public Nave() {
        this.coordenadas = new ArrayList<>();
        this.impactos = 0;
        this.estadoNave = EstadoNave.ACTIVA;
    }

    public Nave(TipoNave tipoNave, List<String> coordenadas, int impactos, EstadoNave estadoNave) {
        this.tipoNave = tipoNave;
        this.coordenadas = coordenadas == null ? new ArrayList<>() : new ArrayList<>(coordenadas);
        this.impactos = impactos;
        this.estadoNave = estadoNave;
        this.colocada = coordenadas != null && !coordenadas.isEmpty();
    }

    public TipoNave getTipoNave() {
        return tipoNave;
    }

    public void setTipoNave(TipoNave tipoNave) {
        this.tipoNave = tipoNave;
    }

    public List<String> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<String> coordenadas) {
        this.coordenadas = coordenadas == null ? new ArrayList<>() : new ArrayList<>(coordenadas);
    }

    public int getImpactos() {
        return impactos;
    }

    public void setImpactos(int impactos) {
        this.impactos = impactos;
    }

    public EstadoNave getEstadoNave() {
        return estadoNave;
    }

    public void setEstadoNave(EstadoNave estadoNave) {
        this.estadoNave = estadoNave;
    }

    public TipoNave getTipo() {
        return getTipoNave();
    }

    public int getTamanio() {
        if (tipoNave == null) {
            return 0;
        }
        switch (tipoNave) {
            case BARCO:
                return 1;   //BARCO = 1
            case SUBMARINO:
                return 2;   //SUBMARINO = 2
            case CRUCERO:
                return 3;   //CRUCERO = 3
            case PORTA_AVIONES:
                return 4;   //PORTA AVIONES = 4
            default:
                return 0;
        }
    }

    public boolean isColocada() {
        return colocada;
    }

    public void setColocada(boolean colocada) {
        this.colocada = colocada;
    }
}
