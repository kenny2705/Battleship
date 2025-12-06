package battleship.application;

import battleship.domain.model.*;
import battleship.domain.enums.*;
import battleship.domain.repository.PartidaRepository;

public class PartidaService {
    private final PartidaRepository partidaRepository;

    public PartidaService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    public Partida iniciarNuevaPartida(String nombreJugador1, String nombreJugador2) {
        Jugador jugador1 = new Jugador(nombreJugador1, "Rojo");
        Jugador jugador2 = new Jugador(nombreJugador2, "Azul");
        
        Partida partida = new Partida();
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);
        partida.iniciar();
        
        partidaRepository.guardar(partida);
        return partida;
    }

    public ResultadoDisparo realizarDisparo(String partidaId, String jugadorId, String coordenada) {
        Partida partida = partidaRepository.buscarPorId(partidaId);
        Jugador atacante = partida.getJugadores().stream()
            .filter(j -> j.getNombre().equals(jugadorId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
            
        Jugador oponente = partida.getJugadores().stream()
            .filter(j -> !j.getNombre().equals(jugadorId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Oponente no encontrado"));
            
        return partida.disparar(atacante, oponente, coordenada);
    }
}