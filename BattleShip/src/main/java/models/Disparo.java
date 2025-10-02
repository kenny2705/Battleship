package models;

import models.enums.ResultadoDisparo;

/**
 *
 * @author Usuario
 */
public class Disparo {
    String coordenada;
    private ResultadoDisparo resultadoDisparo;

    public Disparo() {
    }

    public Disparo(String coordenada, ResultadoDisparo resultadoDisparo) {
        this.coordenada = coordenada;
        this.resultadoDisparo = resultadoDisparo;
    }

    public String getCoordenada() {
        return coordenada;
    }

    public void setCoordenada(String coordenada) {
        this.coordenada = coordenada;
    }

    public ResultadoDisparo getResultadoDisparo() {
        return resultadoDisparo;
    }

    public void setResultadoDisparo(ResultadoDisparo resultadoDisparo) {
        this.resultadoDisparo = resultadoDisparo;
    }
    
    
}
