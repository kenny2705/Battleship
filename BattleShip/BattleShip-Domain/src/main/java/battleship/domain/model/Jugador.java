package battleship.domain.model;

import battleship.domain.enums.ResultadoDisparo;
import java.util.ArrayList;
import java.util.List;

public class Jugador {

    private String nombre;
    private String color;
    private boolean turno;
    private Marcador marcador;
    private List<Tablero> tableros;

    public Jugador() {
        this.marcador = new Marcador();
        this.tableros = new ArrayList<>();

        Tablero tableroDefault = new Tablero(10);
        tableroDefault.setJugador(this);
        this.tableros.add(tableroDefault);
    }

    public Jugador(String nombre, String color) {
        System.out.println("🎮 CONSTRUCTOR Jugador: " + nombre);
        this.nombre = nombre;
        this.color = color;
        this.marcador = new Marcador();
        this.turno = false;

        this.tableros = new ArrayList<>();

        Tablero tableroDefault = new Tablero(10);
        tableroDefault.setJugador(this);
        this.tableros.add(tableroDefault);

        System.out.println("✅ Jugador '" + nombre + "' creado CON tablero incluido");
    }

    public ResultadoDisparo realizarDisparo(String coordenada, Tablero tableroOponente) {
        if (!turno) {
            throw new IllegalStateException("No es el turno del jugador: " + nombre);
        }

        if (tableros == null || tableros.isEmpty()) {
            throw new IllegalStateException("El jugador " + nombre + " no tiene tablero asignado");
        }

        ResultadoDisparo resultado = tableroOponente.recibirDisparo(coordenada);
        marcador.registrarDisparo(resultado);

        return resultado;
    }

    public void colocarFlota(List<Nave> flota) {
        if (tableros == null || tableros.isEmpty()) {
            throw new IllegalStateException("El jugador " + nombre + " no tiene tablero para colocar flota");
        }

        Tablero tablero = tableros.get(0);
        tablero.setNaves(flota);
        tablero.colocarNavesEnCasillas();

        marcador.inicializarNaves(flota.size());
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isTurno() {
        return turno;
    }

    public void setTurno(boolean turno) {
        this.turno = turno;
    }

    public Marcador getMarcador() {
        return marcador;
    }

    public void setMarcador(Marcador marcador) {
        this.marcador = marcador;
    }

    public List<Tablero> getTableros() {
        return tableros;
    }

    public void setTableros(List<Tablero> tableros) {
        this.tableros = tableros;
    }
}
