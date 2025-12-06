package battleship.domain.model;

import battleship.domain.enums.ResultadoDisparo;

public class Marcador {

    private int navesIntactas;
    private int navesAveriadas;
    private int navesHundidas;
    private int totalDisparos;
    private int aciertos;

    public Marcador() {
        this.navesIntactas = 0;
        this.navesAveriadas = 0;
        this.navesHundidas = 0;
        this.totalDisparos = 0;
        this.aciertos = 0;
    }

    public int calculaPuntos() {
        return (navesAveriadas * 10) + (navesHundidas * 50) + (aciertos * 5);
    }

    public void registrarDisparo(ResultadoDisparo resultado) {
        totalDisparos++;

        switch (resultado) {
            case IMPACTO:
                aciertos++;
                navesAveriadas++;
                navesIntactas = Math.max(0, navesIntactas - 1);
                break;
            case HUNDIDO:
                aciertos++;
                navesHundidas++;
                navesAveriadas = Math.max(0, navesAveriadas - 1);
                break;
            case AGUA:
                break;
            case REPETIDO:
                totalDisparos--;
                break;
        }
    }

    public void inicializarNaves(int totalNaves) {
        this.navesIntactas = totalNaves;
        this.navesAveriadas = 0;
        this.navesHundidas = 0;
    }

    public int getNavesIntactas() {
        return navesIntactas;
    }

    public void setNavesIntactas(int navesIntactas) {
        this.navesIntactas = navesIntactas;
    }

    public int getNavesAveriadas() {
        return navesAveriadas;
    }

    public void setNavesAveriadas(int navesAveriadas) {
        this.navesAveriadas = navesAveriadas;
    }

    public int getNavesHundidas() {
        return navesHundidas;
    }

    public void setNavesHundidas(int navesHundidas) {
        this.navesHundidas = navesHundidas;
    }

    public int getTotalDisparos() {
        return totalDisparos;
    }

    public void setTotalDisparos(int totalDisparos) {
        this.totalDisparos = totalDisparos;
    }

    public int getAciertos() {
        return aciertos;
    }

    public void setAciertos(int aciertos) {
        this.aciertos = aciertos;
    }
}
