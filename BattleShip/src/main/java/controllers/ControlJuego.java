package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Casilla;
import models.Jugador;
import models.Disparo;
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

    private Jugador jugador;            // este jugador (local)
    private Tablero oponenteTablero;    // representacion local del tablero rival

    private ControlVista controlVista;

    private final Map<TipoNave, Integer> cuotaMax = new HashMap<TipoNave, Integer>();

    public ControlJuego() {
        inicializarCuotas();
        crearJugadorYTablero();
    }

    public void setControlVista(ControlVista cv) {
        this.controlVista = cv;
    }

    public Jugador getJugador() {
        return jugador;
    }

    private void inicializarCuotas() {
        cuotaMax.put(TipoNave.PORTA_AVIONES, 2);
        cuotaMax.put(TipoNave.CRUCERO, 2);
        cuotaMax.put(TipoNave.SUBMARINO, 4);
        cuotaMax.put(TipoNave.BARCO, 3);
    }

    private void crearJugadorYTablero() {
        Tablero t = new Tablero(10, null);
        t.inicializarCasillas();

        // --- CORRECCIÓN: Inicializar las naves reales, no una lista vacía ---
        List<Nave> navesIniciales = new ArrayList<Nave>();

        // Recorremos las cuotas para crear los objetos Nave
        for (Map.Entry<TipoNave, Integer> entry : cuotaMax.entrySet()) {
            TipoNave tipo = entry.getKey();
            int cantidad = entry.getValue();

            for (int i = 0; i < cantidad; i++) {
                // Creamos la nave. (Asumiendo constructor: Tipo, Coords, Impactos, Estado)
                navesIniciales.add(new Nave(tipo, new ArrayList<String>(), 0, EstadoNave.ACTIVA));
            }
        }
        t.setNaves(navesIniciales);
        // --------------------------------------------------------------------

        t.setDisparos(new ArrayList<Disparo>());

        this.jugador = new Jugador("Jugador 1", "Rojo", true, null, t);
        t.setJugador(jugador);

        this.oponenteTablero = new Tablero(10, null);
        oponenteTablero.inicializarCasillas();
        oponenteTablero.setNaves(new ArrayList<Nave>());
        oponenteTablero.setDisparos(new ArrayList<Disparo>());
    }

    public boolean verificarAcomodoCompleto(Jugador jugador) {
        if (jugador == null || jugador.getTablero() == null) {
            return false;
        }

        Map<TipoNave, Integer> conteo = new HashMap<TipoNave, Integer>();
        for (Nave n : jugador.getNaves()) {
            if (n.isColocada()) {
                if (conteo.containsKey(n.getTipo())) {
                    conteo.put(n.getTipo(), conteo.get(n.getTipo()) + 1);
                } else {
                    conteo.put(n.getTipo(), 1);
                }
            }
        }

        for (Map.Entry<TipoNave, Integer> entrada : cuotaMax.entrySet()) {
            TipoNave tipo = entrada.getKey();
            int max = entrada.getValue();
            int actuales = conteo.containsKey(tipo) ? conteo.get(tipo) : 0;
            if (actuales < max) {
                return false;
            }
        }
        return true;
    }

    public boolean colocarNave(Tablero tablero, Nave nave, String coordInicio, int orientacion) {
        if (tablero == null || nave == null || coordInicio == null) {
            return false;
        }
        coordInicio = coordInicio.trim().toUpperCase();
        int size = nave.getTamanio();
        if (size <= 0) {
            return false;
        }

        char letra = coordInicio.charAt(0);
        int fila = letra - 'A';
        int columna;
        try {
            columna = Integer.parseInt(coordInicio.substring(1)) - 1;
        } catch (NumberFormatException ex) {
            return false;
        }

        int medidas = tablero.getMedidas();
        List<String> coords = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            int f = fila + (orientacion == 1 ? i : 0);
            int c = columna + (orientacion == 0 ? i : 0);
            if (f < 0 || f >= medidas || c < 0 || c >= medidas) {
                return false;
            }
            coords.add("" + (char) ('A' + f) + (c + 1));
        }

        // choque con otras naves
        for (Nave n : tablero.getNaves()) {
            if (n.getCoordenadas() == null || n == nave) { // Ignorar la misma nave si se está moviendo
                continue;
            }
            for (String c : n.getCoordenadas()) {
                if (coords.contains(c.trim().toUpperCase())) {
                    return false;
                }
            }
        }

        nave.setCoordenadas(coords);
        // Si la nave ya estaba en la lista, no necesitamos agregarla de nuevo, solo actualizar estado
        if (!tablero.getNaves().contains(nave)) {
            tablero.agregarNave(nave);
        }

        nave.setColocada(true);
        tablero.colocarNavesEnCasillas(); // Refrescar matriz
        tablero.notificarObservadores("Nave colocada: " + nave.getTipoNave());

        return true;
    }

    public boolean dispararAOponente(String coordenada) {
        if (coordenada == null || controlVista == null) {
            return false;
        }
        coordenada = coordenada.trim().toUpperCase();

        Casilla c = buscarCasillaEnTablero(oponenteTablero, coordenada);
        if (c != null && c.isDañada()) {
            return false;
        }

        if (c != null) {
            c.setDañada(true);
        }

        String msg = "DISPARO:" + coordenada;
        try {
            controlVista.enviarMensaje(msg);
        } catch (Exception ex) {
            if (c != null) {
                c.setDañada(false);
            }
            return false;
        }
        return true;
    }

    public void procesarMensajeEntrante(String msg) {
        if (msg == null) {
            return;
        }
        msg = msg.trim();
        if (msg.startsWith("DISPARO:")) {
            String coord = msg.substring("DISPARO:".length()).trim().toUpperCase();
            procesarDisparoRemoto(coord);
        } else if (msg.startsWith("RESULT:")) {
            String[] parts = msg.split(":");
            if (parts.length >= 3) {
                String coord = parts[1].trim().toUpperCase();
                String resStr = parts[2].trim().toUpperCase();
                ResultadoDisparo resultado = ResultadoDisparo.valueOf(resStr);
                procesarResultadoDisparo(coord, resultado);
            }
        } else if (msg.startsWith("ACOMODO_LISTO")) {
            if (controlVista != null) {
                controlVista.notificarRivalListo();
            }
        }
    }

    private void procesarDisparoRemoto(String coord) {
        Casilla cas = buscarCasillaEnTablero(jugador.getTablero(), coord);
        if (cas == null) {
            return;
        }
        if (cas.isDañada()) {
            ResultadoDisparo r = cas.isOcupada() ? ResultadoDisparo.IMPACTO : ResultadoDisparo.AGUA;
            enviarResultado(coord, r);
            return;
        }

        cas.setDañada(true);

        ResultadoDisparo resultado = cas.isOcupada() ? ResultadoDisparo.IMPACTO : ResultadoDisparo.AGUA;

        if (resultado == ResultadoDisparo.IMPACTO) {
            boolean hundido = comprobarHundimiento(coord);
            if (hundido) {
                resultado = ResultadoDisparo.HUNDIDO;
            }
        }

        jugador.getTablero().getDisparos().add(new Disparo(coord, resultado));

        jugador.getTablero().notificarObservadores("Disparo entrante en " + coord + ": " + resultado);
        enviarResultado(coord, resultado);
    }

    private void enviarResultado(String coord, ResultadoDisparo resultado) {
        if (controlVista == null) {
            return;
        }
        String msg = "RESULT:" + coord + ":" + resultado.name();
        try {
            controlVista.enviarMensaje(msg);
        } catch (Exception ex) {
            System.err.println("Error enviando resultado: " + ex.getMessage());
        }
    }

    private void procesarResultadoDisparo(String coord, ResultadoDisparo resultado) {
        Casilla cas = buscarCasillaEnTablero(oponenteTablero, coord);
        if (cas == null) {
            return;
        }
        cas.setDañada(true);
        if (resultado == ResultadoDisparo.IMPACTO || resultado == ResultadoDisparo.HUNDIDO) {
            cas.setOcupada(true);
        }

        oponenteTablero.getDisparos().add(new Disparo(coord, resultado));

        oponenteTablero.notificarObservadores("Resultado disparo " + coord + ": " + resultado);
    }

    private Casilla buscarCasillaEnTablero(Tablero tablero, String coord) {
        if (tablero == null || coord == null) {
            return null;
        }
        coord = coord.trim().toUpperCase();
        for (Casilla c : tablero.getMatrizDeCasillas()) {
            if (c.getCoordenada().equalsIgnoreCase(coord)) {
                return c;
            }
        }
        return null;
    }

    private boolean comprobarHundimiento(String coord) {
        if (coord == null) {
            return false;
        }
        for (Nave n : jugador.getTablero().getNaves()) {
            if (n.getCoordenadas() == null) {
                continue;
            }
            for (String c : n.getCoordenadas()) {
                if (c.trim().equalsIgnoreCase(coord)) {
                    boolean todasDaniadas = true;
                    for (String co : n.getCoordenadas()) {
                        Casilla cas = buscarCasillaEnTablero(jugador.getTablero(), co);
                        if (cas == null || !cas.isDañada()) {
                            todasDaniadas = false;
                            break;
                        }
                    }
                    if (todasDaniadas) {
                        n.setEstadoNave(models.enums.EstadoNave.HUNDIDA);
                        return true;
                    } else {
                        n.setEstadoNave(models.enums.EstadoNave.DAÑADA);
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public Tablero getOponenteTablero() {
        return oponenteTablero;
    }
}
