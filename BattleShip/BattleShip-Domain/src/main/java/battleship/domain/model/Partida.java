package battleship.domain.model;

import battleship.domain.enums.EstadoPartida;
import battleship.domain.enums.ResultadoDisparo;
import java.util.ArrayList;
import java.util.List;

public class Partida {

    private String id;
    private EstadoPartida estadoPartida;
    private Jugador ganador;
    private int turno;
    private final List<Jugador> jugadores;
    private final List<PartidaObservador> observadores = new ArrayList<>(); // ✅ NUEVO

    public Partida() {
        this.estadoPartida = EstadoPartida.ACTIVA;
        this.turno = 0;
        this.jugadores = new ArrayList<>();
    }

    public void agregarObservador(PartidaObservador observador) {
        observadores.add(observador);
    }

    public void notificarPartidaFinalizada(Jugador ganador) {
        for (PartidaObservador observador : observadores) {
            observador.onPartidaFinalizada(ganador);
        }
    }

    public void notificarTurnoCambiado(Jugador jugadorConTurno) {
        for (PartidaObservador observador : observadores) {
            observador.onTurnoCambiado(jugadorConTurno);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void iniciar() {
        this.estadoPartida = EstadoPartida.ACTIVA;
        this.turno = 0;
        this.ganador = null;

        for (Jugador jugador : jugadores) {
            if (jugador.getTableros() != null && !jugador.getTableros().isEmpty()) {
                Tablero tablero = jugador.getTableros().get(0);
                jugador.getMarcador().inicializarNaves(tablero.getNaves().size());
            }
        }

        if (!jugadores.isEmpty()) {
            jugadores.get(0).setTurno(true);
            if (jugadores.size() > 1) {
                jugadores.get(1).setTurno(false);
            }
            notificarTurnoCambiado(getJugadorConTurno());
        }
    }

    public ResultadoDisparo disparar(Jugador atacante, Jugador objetivo, String coordenada) {
        if (estadoPartida != EstadoPartida.ACTIVA) {
            throw new IllegalStateException("La partida no está activa");
        }

        if (!atacante.isTurno()) {
            throw new IllegalStateException("No es el turno de " + atacante.getNombre());
        }

        Tablero tableroObjetivo = objetivo.getTableros().get(0);
        ResultadoDisparo resultado = atacante.realizarDisparo(coordenada, tableroObjetivo);

        if (resultado == ResultadoDisparo.AGUA || resultado == ResultadoDisparo.REPETIDO) {
            cambiarTurno();
            System.out.println("Turno cambiado a: " + getJugadorConTurno().getNombre());
            notificarTurnoCambiado(getJugadorConTurno());
        } else {
            System.out.println(atacante.getNombre() + " mantiene el turno (impacto/hundido)");
        }

        if (tableroObjetivo.todasLasNavesHundidas()) {
            this.ganador = atacante;
            finalizar();
            anunciarGanador();
            notificarPartidaFinalizada(ganador);
        }

        return resultado;
    }

    private void cambiarTurno() {
        for (Jugador jugador : jugadores) {
            jugador.setTurno(!jugador.isTurno());
        }
        turno++;
    }

    public Jugador getJugadorConTurno() {
        for (Jugador jugador : jugadores) {
            if (jugador.isTurno()) {
                return jugador;
            }
        }
        return null;
    }

    public void agregarJugador(Jugador jugador) {
        if (jugadores.size() >= 2) {
            throw new IllegalStateException("Máximo 2 jugadores permitidos");
        }
        jugadores.add(jugador);
    }

    public EstadoPartida getEstadoPartida() {
        return estadoPartida;
    }

    public void setEstadoPartida(EstadoPartida estadoPartida) {
        this.estadoPartida = estadoPartida;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public void setGanador(Jugador ganador) {
        this.ganador = ganador;
    }

    public int getTurno() {
        return turno;
    }

    public void setTurno(int turno) {
        this.turno = turno;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public void finalizar() {
        this.estadoPartida = EstadoPartida.FINALIZADA;
    }

    public void anunciarGanador() {
        if (ganador != null) {
            System.out.println("🎉 ¡EL GANADOR ES: " + ganador.getNombre() + "! 🎉");
        }
    }
}
