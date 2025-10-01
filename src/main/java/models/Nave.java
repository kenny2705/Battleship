/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

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

    public Nave() {
    }

    public Nave(TipoNave tipoNave, List coordenadas, int impactos, EstadoNave estadoNave) {
        this.tipoNave = tipoNave;
        this.coordenadas = coordenadas;
        this.impactos = impactos;
        this.estadoNave = estadoNave;
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
        this.coordenadas = coordenadas;
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
}
