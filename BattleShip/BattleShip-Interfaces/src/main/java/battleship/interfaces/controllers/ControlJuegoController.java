package battleship.interfaceadapters.controller;

import battleship.application.ControlJuego;
import battleship.domain.enums.ResultadoDisparo;
import battleship.domain.model.Jugador;

public class ControlJuegoController {

    private final ControlJuego controlJuego;

    public ControlJuegoController(ControlJuego controlJuego) {
        this.controlJuego = controlJuego;
    }

    public ResultadoDisparo procesarDisparo(Jugador jugador, String coordenada) {
        try {
            if (jugador == controlJuego.getJugadorA()) {
                return controlJuego.dispararDesdeA(coordenada);
            } else {
                return controlJuego.dispararDesdeB(coordenada);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar disparo: " + e.getMessage(), e);
        }
    }

    public boolean esTurnoDe(Jugador jugador) {
        return jugador.isTurno();
    }

    public Jugador obtenerOponente(Jugador jugadorActual) {
        return (jugadorActual == controlJuego.getJugadorA())
                ? controlJuego.getJugadorB()
                : controlJuego.getJugadorA();
    }
}
