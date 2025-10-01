/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import models.Casilla;
import models.Jugador;
import models.Nave;
import models.Tablero;
import models.enums.EstadoNave;
import models.enums.ResultadoDisparo;
import models.enums.TipoNave;

/**
 *
 * @author Usuario
 */
public class ControlJuego {
    
    private Jugador jugador;

    public ControlJuego() {
        creaTablero();
    }
    
    public ControlJuego(Jugador jugador) {
        creaTablero();
    }
    
    private void creaTablero(){
        //Crea el primer tablero
        Tablero tablero1 = new Tablero();
        tablero1.setMedidas(10);
        tablero1.setMatrizDeCasillas(new ArrayList<>());
        tablero1.setNaves(new ArrayList<>());
        tablero1.setDisparos(new ArrayList<>());
        
        //Distribucion de las naves
        List<Nave> navesTablero1 = new ArrayList<>();
        navesTablero1.add(new Nave(TipoNave.BARCO, List.of("A1"), 0, EstadoNave.ACTIVA));
        navesTablero1.add(new Nave(TipoNave.BARCO, List.of("A3"), 0, EstadoNave.ACTIVA));
        navesTablero1.add(new Nave(TipoNave.SUBMARINO, List.of("C1", "C2"), 0, EstadoNave.ACTIVA));
        navesTablero1.add(new Nave(TipoNave.SUBMARINO, List.of("C5", "C6"), 0, EstadoNave.ACTIVA));
        navesTablero1.add(new Nave(TipoNave.CRUCERO, List.of("E1", "E2", "E3"), 0, EstadoNave.ACTIVA));
        navesTablero1.add(new Nave(TipoNave.CRUCERO, List.of("E7", "E8", "E9"), 0, EstadoNave.ACTIVA));
        navesTablero1.add(new Nave(TipoNave.PORTA_AVIONES, List.of("H1", "H2", "H3", "H4"), 0, EstadoNave.ACTIVA));
        navesTablero1.add(new Nave(TipoNave.PORTA_AVIONES, List.of("H6", "H7", "H8", "H9"), 0, EstadoNave.ACTIVA));
        tablero1.setNaves(navesTablero1);
        
        //Crea el segundo tablero
        Tablero tablero2 = new Tablero();
        tablero2.setMedidas(10);
        tablero2.setMatrizDeCasillas(new ArrayList<>());
        tablero2.setNaves(new ArrayList<>());
        tablero2.setDisparos(new ArrayList<>());
        
        //Lista de tableros
        List<Tablero> tableros = new ArrayList<>();
        tableros.add(tablero1);
        tableros.add(tablero2);
        
        this.jugador = new Jugador("Jugador 1", "Rojo", true, null, tableros);
    }
    
    public Jugador getJugador(){
        return jugador;
    }
    
    public ResultadoDisparo realizarDisparo (Tablero tablero, String coordenada){
         // Buca una casilla
        Optional<Casilla> casillaOpt = tablero.getMatrizDeCasillas().stream()
                .filter(c -> c.getCoordenada().equals(coordenada))
                .findFirst();

        if (casillaOpt.isEmpty()) {
            return ResultadoDisparo.AGUA; // Coordenada inválida → lo tratamos como agua
        }

        Casilla casilla = casillaOpt.get();

        if (casilla.isOcupada()) {
            casilla.setDañada(true);

            // Buscar la nave a la que pertenece esta casilla
            for (Nave nave : tablero.getNaves()) {
                if (nave.getCoordenadas().contains(coordenada)) {
                    nave.setImpactos(nave.getImpactos() + 1);

                    if (nave.getImpactos() == nave.getCoordenadas().size()) {
                        nave.setEstadoNave(EstadoNave.HUNDIDA);
                        return ResultadoDisparo.HUNDIDO;
                    } else {
                        nave.setEstadoNave(EstadoNave.DAÑADA);
                        return ResultadoDisparo.IMPACTO;
                    }
                }
            }
        }

        // No había nave en la casilla
        return ResultadoDisparo.AGUA;
    }
}
