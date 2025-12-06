package battleship.domain.model;

import battleship.domain.enums.ResultadoDisparo;
import java.util.ArrayList;
import java.util.List;

public class Tablero {

    private int medidas;
    private Casilla[][] matrizdeCasillas;
    private Jugador jugador;
    private List<Nave> naves;
    private List<Disparo> disparos;
    private final List<TableroObservador> observadores = new ArrayList<>();

    public Tablero() {
        this.medidas = 10;
        this.naves = new ArrayList<>();
        this.disparos = new ArrayList<>();
        inicializarCasillas();
    }

    public Tablero(int medidas) {
        this.medidas = medidas;
        this.naves = new ArrayList<>();
        this.disparos = new ArrayList<>();
        inicializarCasillas();
    }

    public boolean validarPosicion(String coordenada) {
        try {
            int[] pos = parseCoord(coordenada);
            int fila = pos[0], col = pos[1];
            return fila >= 0 && fila < medidas && col >= 0 && col < medidas;
        } catch (Exception e) {
            return false;
        }
    }

    public void actualizarCasilla(String coordenada, boolean impacto) {
        if (!validarPosicion(coordenada)) {
            throw new IllegalArgumentException("Coordenada inválida: " + coordenada);
        }

        int[] pos = parseCoord(coordenada);
        Casilla casilla = matrizdeCasillas[pos[0]][pos[1]];

        if (impacto) {
            casilla.marcarImpacto();
        } else {
            casilla.marcarAgua();
        }
    }

    public ResultadoDisparo recibirDisparo(String coordenada) {
        if (!validarPosicion(coordenada)) {
            throw new IllegalArgumentException("Coordenada inválida: " + coordenada);
        }

        for (Disparo disparo : disparos) {
            if (disparo.getCoordenada().equals(coordenada)) {
                notificarObservadores("Disparo repetido en " + coordenada);
                return ResultadoDisparo.REPETIDO;
            }
        }

        Disparo nuevoDisparo = new Disparo(coordenada);
        disparos.add(nuevoDisparo);

        int[] pos = parseCoord(coordenada);
        Casilla casilla = matrizdeCasillas[pos[0]][pos[1]];

        ResultadoDisparo resultado = nuevoDisparo.evaluarResultado(casilla);
        nuevoDisparo.setResultadoDisparo(resultado);

        actualizarCasilla(coordenada, resultado == ResultadoDisparo.IMPACTO || resultado == ResultadoDisparo.HUNDIDO);

        if (resultado == ResultadoDisparo.IMPACTO || resultado == ResultadoDisparo.HUNDIDO) {
            Nave nave = obtenerNaveEn(coordenada);
            if (nave != null) {
                nave.recibirImpacto(coordenada);
                if (nave.naveHundida()) {
                    resultado = ResultadoDisparo.HUNDIDO;
                    nuevoDisparo.setResultadoDisparo(resultado);
                }
            }
        }

        String mensaje = resultado == ResultadoDisparo.HUNDIDO ? "¡HUNDIDO! "
                : resultado == ResultadoDisparo.IMPACTO ? "Impacto en "
                        : "Agua en ";
        notificarObservadores(mensaje + coordenada);

        return resultado;
    }

    public Nave obtenerNaveEn(String coordenada) {
        for (Nave nave : naves) {
            if (nave.ocupa(coordenada)) {
                return nave;
            }
        }
        return null;
    }

    public boolean todasLasNavesHundidas() {
        if (naves == null || naves.isEmpty()) {
            return false;
        }
        
        for (Nave nave : naves) {
            if (!nave.estaHundida()) {
                return false;
            }
        }
        return true;
    }
    
    public void inicializarCasillas() {
        matrizdeCasillas = new Casilla[medidas][medidas];
        for (int i = 0; i < medidas; i++) {
            for (int j = 0; j < medidas; j++) {
                String coord = (i + 1) + "," + (j + 1);
                matrizdeCasillas[i][j] = new Casilla(coord, false, false);
            }
        }
    }

    public void colocarNavesEnCasillas() {
        for (Nave nave : naves) {
            for (String coord : nave.getCoordenadas()) {
                int[] pos = parseCoord(coord);
                matrizdeCasillas[pos[0]][pos[1]].setOcupada(true);
            }
        }
    }

    public void addObservador(TableroObservador observador) {
        observadores.add(observador);
    }

    public void notificarObservadores(String mensaje) {
        for (TableroObservador observador : observadores) {
            observador.actualizarTablero(this, mensaje);
        }
    }

    private int[] parseCoord(String coordenada) {
        String[] parts = coordenada.split(",");
        int fila = Integer.parseInt(parts[0].trim()) - 1;
        int col = Integer.parseInt(parts[1].trim()) - 1;
        return new int[]{fila, col};
    }

    public int getMedidas() {
        return medidas;
    }

    public void setMedidas(int medidas) {
        this.medidas = medidas;
    }

    public Casilla getCasilla(int fila, int col) {
        return matrizdeCasillas[fila][col];
    }

    public List<Nave> getNaves() {
        return naves;
    }

    public void setNaves(List<Nave> naves) {
        this.naves = naves;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public List<Disparo> getDisparos() {
        return disparos;
    }
}
