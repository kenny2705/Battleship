/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package battleship.domain.model;

import battleship.domain.model.Jugador;

public interface PartidaObservador {
    void onPartidaFinalizada(Jugador ganador);
    void onTurnoCambiado(Jugador jugadorConTurno);
}
