package battleship.domain.model;

public class Casilla {

    private String coordenada;
    private boolean ocupada;
    private boolean dañado; // Usando el nombre del diagrama

    public Casilla() {
    }

    public Casilla(String coordenada, boolean ocupada, boolean dañado) {
        this.coordenada = coordenada;
        this.ocupada = ocupada;
        this.dañado = dañado;
    }

    public String obtenerEstado() {
        if (dañado) {
            return ocupada ? "IMPACTO" : "AGUA";
        }
        return ocupada ? "OCUPADA" : "LIBRE";
    }

    public void marcarImpacto() {
        this.dañado = true;
        this.ocupada = true;
    }

    public void marcarAgua() {
        this.dañado = true;
        this.ocupada = false;
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
        return dañado;
    }

    public void setDañada(boolean dañado) {
        this.dañado = dañado;
    }
}
