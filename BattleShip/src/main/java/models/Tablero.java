package models;

import java.util.ArrayList;
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

    private final List<TableroObservador> observadores = new ArrayList<>();

    public Tablero(int medidas, Jugador jugador) {
        this.medidas = medidas;
        this.jugador = jugador;
        this.matrizDeCasillas = new ArrayList<>();
        this.naves = new ArrayList<>();
        this.disparos = new ArrayList<>();
        inicializarCasillas();
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

    public void setMatrizDeCasillas(List<Casilla> lista) {
        this.matrizDeCasillas = lista;
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

    public void inicializarCasillas() {
        matrizDeCasillas.clear();
        for (int fila = 0; fila < medidas; fila++) {
            for (int col = 0; col < medidas; col++) {
                String coordenada = "" + (char) ('A' + fila) + (col + 1);
                matrizDeCasillas.add(new Casilla(coordenada, false, false));
            }
        }
    }
    public boolean agregarNave(Nave nave) {
        if (nave == null || nave.getCoordenadas() == null) {
            return false;
        }
        List<String> coordsNorm = new ArrayList<>();
        for (String c : nave.getCoordenadas()) {
            coordsNorm.add(c.trim().toUpperCase());
        }
        // choca con otras naves
        for (Nave n : naves) {
            if (n.getCoordenadas() == null) {
                continue;
            }
            for (String c : n.getCoordenadas()) {
                if (coordsNorm.contains(c.trim().toUpperCase())) {
                    return false;
                }
            }
        }

        // agrega y marca casillas
        naves.add(nave);
        nave.setColocada(true);
        for (String coord : coordsNorm) {
            Casilla cas = buscarCasilla(coord);
            if (cas != null) {
                cas.setOcupada(true);
            }
        }
        return true;
    }

    public void colocarNavesEnCasillas() {
        // limpiar primero
        for (Casilla c : matrizDeCasillas) {
            c.setOcupada(false);
        }

        if (naves == null) {
            return;
        }
        for (Nave n : naves) {
            if (n.getCoordenadas() == null) {
                continue;
            }
            for (String coord : n.getCoordenadas()) {
                Casilla c = buscarCasilla(coord);
                if (c != null) {
                    c.setOcupada(true);
                }
            }
        }
        notificarObservadores("Naves colocadas");
    }

    private Casilla buscarCasilla(String coord) {
        if (coord == null) {
            return null;
        }
        String cNorm = coord.trim().toUpperCase();
        for (Casilla c : matrizDeCasillas) {
            if (c.getCoordenada().equalsIgnoreCase(cNorm)) {
                return c;
            }
        }
        return null;
    }

    // metodos observadores para tablero.
    public void addObservador(TableroObservador obs) {
        observadores.add(obs);
    }

    public void removeObservador(TableroObservador obs) {
        observadores.remove(obs);
    }

    public void notificarObservadores(String mensaje) {
        for (TableroObservador obs : observadores) {
            obs.actualizarTablero(this, mensaje);
        }
    }
}
