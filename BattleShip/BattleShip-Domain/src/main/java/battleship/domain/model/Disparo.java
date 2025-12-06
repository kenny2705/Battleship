package battleship.domain.model;

import battleship.domain.enums.ResultadoDisparo;

public class Disparo {

    private String coordenada;
    private ResultadoDisparo resultadoDisparo;

    public Disparo() {
    }

    public Disparo(String coordenada) {
        this.coordenada = coordenada;
        this.resultadoDisparo = ResultadoDisparo.AGUA;
    }

    public Disparo(String coordenada, ResultadoDisparo resultadoDisparo) {
        this.coordenada = coordenada;
        this.resultadoDisparo = resultadoDisparo;
    }

    public ResultadoDisparo evaluarResultado(Casilla casilla) {
        if (casilla.isDañada()) {
            return ResultadoDisparo.REPETIDO;
        }

        if (casilla.isOcupada()) {
            return ResultadoDisparo.IMPACTO;
        }

        return ResultadoDisparo.AGUA;
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
