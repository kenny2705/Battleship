package battleship.infrastructure.persistence;

import battleship.domain.model.Partida;
import battleship.domain.repository.PartidaRepository;
import java.util.HashMap;
import java.util.Map;

public class PartidaRepositoryEnMemoria implements PartidaRepository {
    private final Map<String, Partida> partidas = new HashMap<>();
    private int contador = 0;

    @Override
    public Partida buscarPorId(String id) {
        Partida partida = partidas.get(id);
        if (partida == null) {
            throw new PartidaNoEncontradaException("Partida con ID " + id + " no encontrada");
        }
        return partida;
    }

    @Override
    public void guardar(Partida partida) {
        if (partida.getId() == null) {
            partida.setId("PARTIDA_" + (++contador));
        }
        partidas.put(partida.getId(), partida);
    }

    @Override
    public void eliminar(String id) {
        partidas.remove(id);
    }

    public static class PartidaNoEncontradaException extends RuntimeException {
        public PartidaNoEncontradaException(String mensaje) {
            super(mensaje);
        }
    }
}