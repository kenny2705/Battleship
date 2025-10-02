package models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class Tablero{
    private int medidas;
    private List<Casilla> matrizDeCasillas;
    private Jugador jugador;
    private List<Nave> naves;
    private List<Disparo> disparos;

    //Logica del Patron observador parte 1
    private final List<TableroObservador> observadores = new ArrayList<>();
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
            for (String coordenada : nave.getCoordenadas()) {
                for (Casilla casilla : matrizDeCasillas) {
                    if (casilla.getCoordenada().equals(coordenada)) {
                        casilla.setOcupada(true);
                        break;
                    }
                }
            }
        }
    }
    
    //Logica del patron observador parte2
    public void addObservador(TableroObservador observador){
        observadores.add(observador);
    }
    
    public void removeObservador(TableroObservador observador){
        observadores.remove(observador);
    }
    
    public void notificarObservadores(String mensaje){
        for (TableroObservador observador : observadores) {
            observador.actualizarTablero(this, mensaje);
        }
    }
    
    public void inicializarCasillas() {
    matrizDeCasillas = new ArrayList<>();
    for (int fila = 0; fila < medidas; fila++) {
        for (int col = 0; col < medidas; col++) {
            String coordenada = "" + (char)('A' + fila) + (col + 1);
            matrizDeCasillas.add(new Casilla(coordenada, false, false));
        }
    }
}

}
