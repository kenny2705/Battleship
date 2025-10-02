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
    
    public ResultadoDisparo realizarDisparo(Tablero tablero, String coordenada) {
        Casilla casilla = tablero.getMatrizDeCasillas()
                .stream()
                .filter(c -> c.getCoordenada().equals(coordenada))
                .findFirst()
                .orElseThrow();
        casilla.setDañada(true);

        ResultadoDisparo resultado;
        if (casilla.isOcupada()) {
            resultado = ResultadoDisparo.IMPACTO;
            // Logica para cambiar a HUNDIDO si todas las casillas de la nave estan dañadas
        } else {
            resultado = ResultadoDisparo.AGUA;
        }

        // Notificar a los observadores
        tablero.notificarObservadores("Disparo en " + coordenada + ": " + resultado);

        return resultado;

        //tablero.notificarObservadores("Disparo en " + coordenada);
        //return casilla.isOcupada() ? ResultadoDisparo.IMPACTO : ResultadoDisparo.AGUA;
    }
    
}
