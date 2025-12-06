package battleship.interfaces.controllers;

import battleship.application.ControlJuego;
import battleship.domain.enums.ResultadoDisparo;
import battleship.domain.enums.EstadoPartida;
import battleship.domain.model.*;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller principal que maneja la comunicación entre View y Model Implementa
 * Observer para recibir actualizaciones del Model
 */
public class PartidaController implements TableroObservador, PartidaObservador {

    private final ControlJuego controlJuego;
    private final Jugador jugadorAsociado;
    private final Tablero tableroOponente;
    private final List<ViewUpdateListener> viewListeners = new ArrayList<>();

    // ✅ NUEVA INTERFAZ para actualización específica del tablero
    public interface VistaTableroListener {

        void onTableroActualizado(Tablero tablero);
    }

    private final List<VistaTableroListener> tableroListeners = new ArrayList<>();

    // Interface para notificar a la View general
    public interface ViewUpdateListener {

        void onTurnoActualizado(boolean esMiTurno);

        void onPartidaEstadoActualizado(EstadoPartida estado, String ganador);

        void onErrorOcurrido(String mensaje);

        void onMensajeRecibido(String mensaje);
    }

    public PartidaController(ControlJuego controlJuego, Jugador jugador) {
        this.controlJuego = controlJuego;
        this.jugadorAsociado = jugador;
        this.tableroOponente = controlJuego.getTableroOponente(jugador);

        // Registrar como observador
        tableroOponente.addObservador(this);
        controlJuego.getPartida().agregarObservador(this);

        System.out.println("🎮 PartidaController creado para: " + jugador.getNombre());
        System.out.println("   Observador registrado en tablero oponente");
    }

    // ========== MÉTODOS PARA LA VIEW ==========
    /**
     * La View llama a este método cuando el usuario hace click en una celda
     */
    public ResultadoDisparo procesarDisparo(int fila, int columna) {
        System.out.println("🎯 PartidaController.procesarDisparo(" + fila + "," + columna + ")");

        if (!esPartidaActiva()) {
            notificarError("La partida ha terminado");
            return null;
        }

        if (!esTurnoDelJugador()) {
            notificarError("No es tu turno");
            return null;
        }

        try {
            // ✅ Coordenadas en formato "fila,columna" (1-based)
            String coordenada = (fila + 1) + "," + (columna + 1);
            System.out.println("   Coordenada: " + coordenada);

            ResultadoDisparo resultado;

            if (jugadorAsociado == controlJuego.getJugadorA()) {
                resultado = controlJuego.dispararDesdeA(coordenada);
            } else {
                resultado = controlJuego.dispararDesdeB(coordenada);
            }

            System.out.println("   Resultado: " + resultado);

            notificarMensaje(formatResultadoMensaje(resultado));
            notificarTurnoActualizado(esTurnoDelJugador());

            return resultado;

        } catch (Exception e) {
            System.err.println("❌ Error en procesarDisparo: " + e.getMessage());
            notificarError("Error al disparar: " + e.getMessage());
            return null;
        }
    }

    public ResultadoDisparo procesarDisparo(String coordenada) {
        try {
            // Parsear coordenada "fila,columna" (ej: "3,5")
            String[] partes = coordenada.split(",");
            int fila = Integer.parseInt(partes[0].trim()) - 1; // Convertir a 0-based
            int columna = Integer.parseInt(partes[1].trim()) - 1;

            return procesarDisparo(fila, columna);
        } catch (Exception e) {
            System.err.println("❌ Error parseando coordenada: " + coordenada);
            notificarError("Coordenada inválida: " + coordenada);
            return null;
        }
    }

    public ControlJuego getControlJuego() {
        return this.controlJuego;
    }

    /**
     * Obtener información para mostrar en la View
     */
    public String getNombreJugador() {
        return jugadorAsociado.getNombre();
    }

    public boolean esTurnoDelJugador() {
        return jugadorAsociado.isTurno();
    }

    public boolean esPartidaActiva() {
        return controlJuego.getPartida().getEstadoPartida() == EstadoPartida.ACTIVA;
    }

    public String getNombreGanador() {
        Jugador ganador = controlJuego.getPartida().getGanador();
        return ganador != null ? ganador.getNombre() : "";
    }

    public Tablero getTableroOponente() {
        return tableroOponente;
    }

    public EstadoPartida getEstadoPartida() {
        return controlJuego.getPartida().getEstadoPartida();
    }

    public Jugador getJugadorAsociado() {
        return jugadorAsociado;
    }

    // ========== REGISTRO DE LISTENERS ==========
    public void addViewUpdateListener(ViewUpdateListener listener) {
        viewListeners.add(listener);
    }

    public void removeViewUpdateListener(ViewUpdateListener listener) {
        viewListeners.remove(listener);
    }

    // ✅ NUEVO: Registrar listeners específicos para el tablero
    public void addVistaTableroListener(VistaTableroListener listener) {
        tableroListeners.add(listener);
        System.out.println("👁️  VistaTableroListener registrado: " + listener.getClass().getSimpleName());
    }

    public void removeVistaTableroListener(VistaTableroListener listener) {
        tableroListeners.remove(listener);
    }

    // ========== NOTIFICACIONES A VIEW ==========
    private void notificarTurnoActualizado(boolean esMiTurno) {
        SwingUtilities.invokeLater(() -> {
            for (ViewUpdateListener listener : viewListeners) {
                listener.onTurnoActualizado(esMiTurno);
            }
        });
    }

    private void notificarPartidaEstadoActualizado() {
        SwingUtilities.invokeLater(() -> {
            EstadoPartida estado = getEstadoPartida();
            String ganador = getNombreGanador();

            for (ViewUpdateListener listener : viewListeners) {
                listener.onPartidaEstadoActualizado(estado, ganador);
            }
        });
    }

    private void notificarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            for (ViewUpdateListener listener : viewListeners) {
                listener.onErrorOcurrido(mensaje);
            }
        });
    }

    private void notificarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            for (ViewUpdateListener listener : viewListeners) {
                listener.onMensajeRecibido(mensaje);
            }
        });
    }

    // ✅ NUEVO: Notificar actualización específica del tablero
    private void notificarTableroActualizado(Tablero tablero) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🔔 Notificando " + tableroListeners.size()
                    + " listeners de tablero actualizado");

            for (VistaTableroListener listener : tableroListeners) {
                try {
                    listener.onTableroActualizado(tablero);
                } catch (Exception e) {
                    System.err.println("Error notificando tablero listener: " + e.getMessage());
                }
            }
        });
    }

    // ========== IMPLEMENTACIÓN DE OBSERVER ==========
    @Override
    public void actualizarTablero(Tablero tablero, String mensaje) {
        System.out.println("🔄 PartidaController.actualizarTablero() - " + mensaje);
        System.out.println("   Tablero recibido: " + (tablero != null ? "OK" : "NULL"));
        System.out.println("   Mensaje: " + mensaje);

        // 1. Enviar mensaje general a la vista
        notificarMensaje(mensaje);

        // 2. ✅ CRÍTICO: Notificar actualización específica del tablero
        notificarTableroActualizado(tablero);

        // 3. Actualizar estado del turno
        notificarTurnoActualizado(esTurnoDelJugador());
    }

    @Override
    public void onPartidaFinalizada(Jugador ganador) {
        System.out.println("🏁 PartidaController.onPartidaFinalizada() - Ganador: " + ganador.getNombre());

        notificarPartidaEstadoActualizado();
        notificarMensaje("🎉 ¡Partida finalizada! Ganador: " + ganador.getNombre());

        // Deshabilitar futuras actualizaciones del tablero
        tableroListeners.clear();
    }

    @Override
    public void onTurnoCambiado(Jugador jugadorConTurno) {
        System.out.println("🔄 PartidaController.onTurnoCambiado() - Turno de: " + jugadorConTurno.getNombre());

        notificarTurnoActualizado(jugadorConTurno == jugadorAsociado);
    }

    // ========== MÉTODOS PRIVADOS DE AYUDA ==========
    private String formatResultadoMensaje(ResultadoDisparo resultado) {
        if (resultado == null) {
            return "";
        }

        return switch (resultado) {
            case AGUA ->
                "💧 ¡Agua! Turno pasado al oponente";
            case IMPACTO ->
                "🎯 ¡Impacto! Sigues disparando";
            case HUNDIDO ->
                "💥 ¡Hundido! Sigues disparando";
            case REPETIDO ->
                "⚠️ Ya disparaste aquí";
        };
    }

    // ✅ NUEVO: Método para forzar actualización del tablero (para debug)
    public void forzarActualizacionTablero() {
        System.out.println("🔧 Forzando actualización del tablero");
        notificarTableroActualizado(tableroOponente);
    }
}
