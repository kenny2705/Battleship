package battleship.domain.repository;

import battleship.domain.model.Partida;

public interface PartidaRepository {

    Partida buscarPorId(String id);

    void guardar(Partida partida);

    void eliminar(String id);
}
