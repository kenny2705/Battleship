package controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import models.Casilla;
import models.Jugador;
import models.Disparo;
import models.Nave;
import models.Tablero;
import models.enums.EstadoNave;
import models.enums.ResultadoDisparo;
import models.enums.TipoNave;

/**
 * CLASE CONTROLADORA QUE MANEJA LA LOGICA DEL JUEGO
 * @author Usuario
 */
public class ControlJuego {

    private Jugador jugador;
    private Tablero oponenteTablero;
    private ControlVista controlVista;
    //Cantidad de naves deacuerdo al tipo
    private final Map<TipoNave, Integer> cuotaMax = new HashMap<TipoNave, Integer>();
    private boolean esMiTurno = false;
    private Timer turnoTimer;
    private int segundosRestantes = 30;

    public ControlJuego() {
        inicializarCuotas();
        crearJugadorYTablero();
        inicializarTemporizador();
    }

    public void setControlVista(ControlVista cv) {
        this.controlVista = cv;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public Tablero getOponenteTablero() {
        return oponenteTablero;
    }

    public boolean esMiTurno() {
        return esMiTurno;
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

        List<Nave> navesIniciales = new ArrayList<Nave>();
        for (Map.Entry<TipoNave, Integer> entry : cuotaMax.entrySet()) {
            TipoNave tipo = entry.getKey();
            int cantidad = entry.getValue();
            for (int i = 0; i < cantidad; i++) {
                // Constructor de Nave( Tipo, Coordenadas (vacias), Impactos, Estado)
                navesIniciales.add(new Nave(tipo, new ArrayList<String>(), 0, EstadoNave.ACTIVA));
            }
        }
        t.setNaves(navesIniciales);
        t.setDisparos(new ArrayList<Disparo>());

        this.jugador = new Jugador("Jugador 1", "Rojo", true, null, t);

        t.setJugador(jugador);

        this.oponenteTablero = new Tablero(10, null);
        oponenteTablero.inicializarCasillas();
        oponenteTablero.setNaves(new ArrayList<Nave>());
        oponenteTablero.setDisparos(new ArrayList<Disparo>());
    }

    private void inicializarTemporizador() {
        turnoTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (segundosRestantes > 0) {
                    segundosRestantes--;
                    if (controlVista != null) {
                        controlVista.actualizarTiempo(segundosRestantes);
                    }
                } else {
                    timeoutTurno();
                }
            }
        });
    }

    public void iniciarTurno() {
        this.esMiTurno = true;
        this.segundosRestantes = 30;
        if (controlVista != null) {
            controlVista.actualizarEstadoTurno("TU TURNO");
            controlVista.habilitarTableroOponente(true);
            controlVista.actualizarTiempo(segundosRestantes);
        }
        turnoTimer.restart();
    }

    public void finalizarTurno() {
        this.esMiTurno = false;
        turnoTimer.stop();
        if (controlVista != null) {
            controlVista.actualizarEstadoTurno("TURNO DEL RIVAL");
            controlVista.habilitarTableroOponente(false);
            controlVista.actualizarTiempo(0);
        }
    }

    private void timeoutTurno() {
        finalizarTurno();
        if (controlVista != null) {
            try {
                controlVista.mostrarMensaje("¡Tiempo agotado! Pierdes el turno.");
                controlVista.enviarMensaje("TIMEOUT");
            } catch (Exception ex) {
                System.err.println("Error enviando timeout: " + ex.getMessage());
            }
        }
    }

    public void iniciarJuegoReal() {
        if (jugador.getNombre().contains("Host") || jugador.getNombre().contains("Jugador 1")) {
            iniciarTurno();
        } else {
            finalizarTurno();
        }
    }

    public boolean dispararAOponente(String coordenada) {
        if (!esMiTurno) {
            if (controlVista != null) {
                controlVista.mostrarMensaje("No es tu turno.");
            }
            return false;
        }
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
        turnoTimer.stop();

        try {
            controlVista.enviarMensaje("DISPARO:" + coordenada);
            return true;
        } catch (Exception ex) {
            if (c != null) {
                c.setDañada(false);
            }
            turnoTimer.start();
            return false;
        }
    }

    public void procesarMensajeEntrante(String msg) {
        if (msg == null) {
            return;
        }
        msg = msg.trim();

        if (msg.startsWith("DISPARO:")) {
            String coord = msg.substring(8).trim().toUpperCase();
            procesarDisparoRemoto(coord);

        } else if (msg.startsWith("RESULT:")) {
            String[] parts = msg.split(":");
            if (parts.length >= 3) {
                String coord = parts[1].trim().toUpperCase();
                ResultadoDisparo resultado = ResultadoDisparo.valueOf(parts[2]);
                procesarResultadoDisparoLocal(coord, resultado);
            }

        } else if (msg.equals("TIMEOUT")) {
            if (controlVista != null) {
                controlVista.mostrarMensaje("El rival agotó su tiempo. ¡Es tu turno!");
            }
            iniciarTurno();

        } else if (msg.startsWith("ACOMODO_LISTO")) {
            if (controlVista != null) {
                controlVista.notificarRivalListo();
            }
            //VICTORIA
        } else if (msg.equals("GAME_OVER")) {
           
            finalizarTurno(); 
            if (controlVista != null) {
                controlVista.terminarPartida(true);
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
            if (comprobarHundimiento(coord)) {
                resultado = ResultadoDisparo.HUNDIDO;
            }
        }

        jugador.getTablero().getDisparos().add(new Disparo(coord, resultado));
        jugador.getTablero().notificarObservadores("Disparo en " + coord + ": " + resultado);

        enviarResultado(coord, resultado);

        // ---DERROTA ---

        if (jugador.haPerdido()) {
            try {
                
                controlVista.enviarMensaje("GAME_OVER");
                controlVista.terminarPartida(false);
            } catch (Exception ex) {
                System.err.println("Error enviando Game Over: " + ex.getMessage());
            }
            return;
        }

        if (resultado == ResultadoDisparo.AGUA) {
            iniciarTurno();
        } else {
            if (controlVista != null) {
                controlVista.actualizarEstadoTurno("RIVAL ACERTO - SIGUE SU TURNO");
            }
        }
    }

    private void procesarResultadoDisparoLocal(String coord, ResultadoDisparo resultado) {
        Casilla cas = buscarCasillaEnTablero(oponenteTablero, coord);
        if (cas != null) {
            cas.setDañada(true);
            if (resultado == ResultadoDisparo.IMPACTO || resultado == ResultadoDisparo.HUNDIDO) {
                cas.setOcupada(true);
            }
            oponenteTablero.getDisparos().add(new Disparo(coord, resultado));
            oponenteTablero.notificarObservadores("Resultado: " + resultado);
        }

        if (resultado == ResultadoDisparo.AGUA) {
            finalizarTurno();
        } else {
            if (controlVista != null) {
                controlVista.mostrarMensaje("¡Acertaste! Tienes otro tiro.");
            }
            segundosRestantes = 30;
            turnoTimer.restart();
        }
    }

    private void enviarResultado(String coord, ResultadoDisparo resultado) {
        if (controlVista == null) {
            return;
        }
        try {
            controlVista.enviarMensaje("RESULT:" + coord + ":" + resultado.name());
        } catch (Exception ex) {
            System.err.println("Error enviando resultado: " + ex.getMessage());
        }
    }

    public boolean verificarAcomodoCompleto(Jugador jugador) {
        if (jugador == null || jugador.getTablero() == null) {
            return false;
        }
        Map<TipoNave, Integer> conteo = new HashMap<TipoNave, Integer>();
        for (Nave n : jugador.getNaves()) {
            if (n.isColocada()) {
                conteo.merge(n.getTipo(), 1, Integer::sum);
            }
        }
        for (Map.Entry<TipoNave, Integer> e : cuotaMax.entrySet()) {
            if (conteo.getOrDefault(e.getKey(), 0) < e.getValue()) {
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

        for (Nave n : tablero.getNaves()) {
            if (n.getCoordenadas() == null || n == nave) {
                continue;
            }
            for (String c : n.getCoordenadas()) {
                if (coords.contains(c.trim().toUpperCase())) {
                    return false;
                }
            }
        }

        nave.setCoordenadas(coords);
        if (!tablero.getNaves().contains(nave)) {
            tablero.agregarNave(nave);
        }

        nave.setColocada(true);
        tablero.colocarNavesEnCasillas();
        tablero.notificarObservadores("Nave colocada: " + nave.getTipoNave());
        return true;
    }

    private Casilla buscarCasillaEnTablero(Tablero tablero, String coord) {
        if (tablero == null || coord == null) {
            return null;
        }
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
}
