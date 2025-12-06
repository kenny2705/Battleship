package battleship.application;

import battleship.application.services.ComunicacionService;
import battleship.application.services.ConnectionListener;
import battleship.application.services.MessageListener;
import battleship.domain.model.*;
import battleship.domain.enums.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import javax.swing.SwingUtilities;

public class ControlJuego {

    private Partida partida;
    private Jugador jugadorLocal;      // Este jugador (yo)
    private Jugador jugadorRemoto;     // El oponente
    private ComunicacionService comunicacionService;
    private boolean soyServidor = false;
    private boolean miTurno = false;
    private String miNombre = "Jugador Local";
    private String nombreOponente = "Oponente";
    private boolean partidaIniciada = false;

    // NUEVAS VARIABLES PARA CONTROL DE COLOCACIÓN
    private boolean colocacionCompletadaLocal = false;
    private boolean colocacionCompletadaRemota = false;
    private boolean partidaListaParaIniciar = false;

    // ✅ INTERFAZ PARA NOTIFICAR A LA CAPA INTERFACES
    public interface PanelJuegoListener {

        void abrirPanelJuego(ControlJuego controlJuego, Jugador jugador);
    }

    private PanelJuegoListener panelJuegoListener;

    // Constructor para modo online (con P2P)
    public ControlJuego(ComunicacionService comunicacionService) {
        this.comunicacionService = comunicacionService;
        configurarComunicacion();

        System.out.println("ControlJuego creado (esperando nombre del jugador)");
    }

    // ✅ SETTER PARA EL LISTENER
    public void setPanelJuegoListener(PanelJuegoListener listener) {
        this.panelJuegoListener = listener;
    }

    private void configurarComunicacion() {
        if (comunicacionService == null) {
            return;
        }

        comunicacionService.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(String message) {
                System.out.println("📨 Mensaje recibido: " + message);
                procesarMensajeRecibido(message);
            }
        });

        comunicacionService.setConnectionListener(new ConnectionListener() {
            @Override
            public void onEvent(String type, String data) {
                switch (type) {
                    case "serverStarted":
                        System.out.println("Servidor iniciado. ID: " + data);
                        break;
                    case "clientConnected":
                        System.out.println("✅ SERVIDOR: Cliente conectado al servidor");

                        enviarMensaje("NOMBRE:" + miNombre);

                        if (jugadorLocal == null) {
                            System.out.println("👤 SERVIDOR: Creando jugador local...");
                            jugadorLocal = new Jugador(miNombre, "Azul");
                            jugadorLocal.setTableros(new ArrayList<>());
                            Tablero tablero = new Tablero(10);
                            tablero.setJugador(jugadorLocal);
                            jugadorLocal.getTableros().add(tablero);
                            System.out.println("✅ SERVIDOR: Jugador creado con tablero");
                        }

                        SwingUtilities.invokeLater(() -> {
                            System.out.println("🚀 SERVIDOR: Abriendo PanelColocacion");

                            if (panelJuegoListener != null) {
                                panelJuegoListener.abrirPanelJuego(ControlJuego.this, jugadorLocal);
                            } else {
                                System.err.println("❌ SERVIDOR: panelJuegoListener es null");
                            }
                        });

                        // 4. Confirmar conexión al cliente
                        enviarMensaje("CONEXION_CONFIRMADA");
                        break;
                    case "connectedToServer":
                        System.out.println("Conectado al servidor oponente");
                        enviarMensaje("NOMBRE:" + miNombre);

                        // SwingUtilities.invokeLater(() -> {
                        //     System.out.println("🚀 CLIENTE: Notificando para abrir PanelColocacion");
                        //     
                        //     if (panelJuegoListener != null) {
                        //         panelJuegoListener.abrirPanelJuego(ControlJuego.this, jugadorLocal);
                        //     }
                        // });
                        break;
                    case "peerDisconnected":
                        System.out.println("Oponente desconectado");
                        break;
                    case "error":
                        System.err.println("Error: " + data);
                        break;
                }
            }
        });
    }

    private void procesarMensajeRecibido(String mensaje) {
        System.out.println("📨 Procesando mensaje: " + mensaje);

        if (mensaje.startsWith("NOMBRE:")) {
            nombreOponente = mensaje.substring(7);
            System.out.println("✅ Nombre del oponente: " + nombreOponente);
            if (soyServidor && !partidaIniciada) {
                iniciarPartidaOnline();
            }
        } else if (mensaje.startsWith("DISPARO:")) {
            String coordenada = mensaje.substring(8);
            procesarDisparoRecibido(coordenada);
        } else if (mensaje.startsWith("RESULTADO:")) {
            String[] partes = mensaje.split(":");
            if (partes.length >= 3) {
                String coordenada = partes[1];
                String resultadoStr = partes[2];
                try {
                    ResultadoDisparo resultado = ResultadoDisparo.valueOf(resultadoStr);
                    mostrarResultadoDisparo(coordenada, resultado);
                } catch (IllegalArgumentException e) {
                    System.err.println("Resultado desconocido: " + resultadoStr);
                }
            }
        } else if (mensaje.equals("TURNO")) {
            miTurno = true;
            System.out.println("🔄 Ahora es mi turno");
        } else if (mensaje.equals("GANADOR")) {
            System.out.println("🏆 ¡Has perdido! El oponente ganó.");
        } else if (mensaje.equals("CONEXION_CONFIRMADA")) {
            System.out.println("✅ Confirmación de conexión recibida del servidor");
            if (!soyServidor) {
                notificarConexionConfirmada();
            }
        } else if (mensaje.equals("COLOCACION_COMPLETADA")) {
            System.out.println("✅ " + nombreOponente + " completó su colocación");
            colocacionCompletadaRemota = true;
            verificarColocacionesCompletadas();
        } else if (mensaje.equals("INICIAR_PARTIDA")) {
            System.out.println("🚀 Recibido INICIAR_PARTIDA del servidor");
            colocacionCompletadaRemota = true;
            partidaListaParaIniciar = true;

            // ✅ CAMBIO: Crear partida en el cliente
            crearPartidaParaCliente();

            // ✅ NOTIFICAR A LA CAPA INTERFACES PARA QUE ABRA PanelJuego
            if (panelJuegoListener != null) {
                panelJuegoListener.abrirPanelJuego(this, jugadorLocal);
            }

            notificarPartidaLista();
        } else {
            System.out.println("⚠️  Mensaje no reconocido: " + mensaje);
        }
    }

    /**
     * Método para que el CLIENTE cree su partida cuando recibe INICIAR_PARTIDA
     */
    private void crearPartidaOnlineCompleta() {
        System.out.println("🎮 Creando partida online COMPLETA...");

        // 1. Verificar que tenemos los nombres REALES
        System.out.println("   Mi nombre: " + miNombre
                + " (es real? " + (miNombre != null && !miNombre.startsWith("Jugador_")) + ")");
        System.out.println("   Oponente: " + nombreOponente);

        // 2. Verificar que el jugador local YA EXISTE con sus naves
        if (jugadorLocal == null) {
            System.err.println("❌ ERROR CRÍTICO: Jugador local NO existe");

            // Crear emergencia SOLO si tenemos nombre real
            if (miNombre != null && !miNombre.startsWith("Jugador_")) {
                jugadorLocal = new Jugador(miNombre, soyServidor ? "Azul" : "Rojo");
                System.out.println("⚠️  Jugador creado de emergencia: " + miNombre);
            } else {
                System.err.println("❌ NO se puede crear jugador: nombre no válido: " + miNombre);
                return;
            }
        } else {
            System.out.println("✅ Jugador local existe: " + jugadorLocal.getNombre());

            // Verificar que tiene tablero con naves
            if (jugadorLocal.getTableros() != null && !jugadorLocal.getTableros().isEmpty()) {
                Tablero tablero = jugadorLocal.getTableros().get(0);
                System.out.println("✅ Tablero existe con "
                        + (tablero.getNaves() != null ? tablero.getNaves().size() : 0) + " naves");
            } else {
                System.err.println("⚠️  Jugador local NO tiene tablero con naves");
            }
        }

        // 3. Crear jugador remoto (oponente)
        if (jugadorRemoto == null) {
            if (nombreOponente != null && !nombreOponente.isEmpty()) {
                jugadorRemoto = new Jugador(nombreOponente, soyServidor ? "Rojo" : "Azul");
                System.out.println("✅ Jugador remoto creado: " + nombreOponente);
            } else {
                System.err.println("❌ No se puede crear jugador remoto: nombre vacío");
                return;
            }
        }

        // 4. Crear tablero VACÍO para jugador remoto (él coloca sus propias naves)
        if (jugadorRemoto.getTableros() == null || jugadorRemoto.getTableros().isEmpty()) {
            Tablero tableroRemoto = new Tablero(10);
            tableroRemoto.setJugador(jugadorRemoto);
            jugadorRemoto.setTableros(new ArrayList<>());
            jugadorRemoto.getTableros().add(tableroRemoto);
            System.out.println("🎯 Tablero vacío creado para oponente");
        }

        // 5. Crear la partida
        if (partida == null) {
            partida = new Partida();
            partida.agregarJugador(jugadorLocal);
            partida.agregarJugador(jugadorRemoto);
            partida.iniciar();
            System.out.println("✅ Partida creada con 2 jugadores");
        } else {
            System.out.println("⚠️  Partida ya existía, no se recrea");
        }

        // 6. Configurar turnos (servidor empieza)
        jugadorLocal.setTurno(soyServidor);
        jugadorRemoto.setTurno(!soyServidor);
        miTurno = soyServidor;

        partidaIniciada = true;

        System.out.println("✅ Partida online COMPLETA creada");
        System.out.println("   Jugador 1: " + jugadorLocal.getNombre()
                + " (Turno: " + jugadorLocal.isTurno() + ")");
        System.out.println("   Jugador 2: " + jugadorRemoto.getNombre()
                + " (Turno: " + jugadorRemoto.isTurno() + ")");
        System.out.println("   Mi turno: " + miTurno);
    }

    private void crearPartidaParaCliente() {
        System.out.println("🎮 Cliente: Creando partida local...");

        if (jugadorLocal == null) {
            jugadorLocal = new Jugador(miNombre, "Rojo");
            System.out.println("👤 Jugador local creado: " + miNombre);
        }

        if (jugadorRemoto == null) {
            jugadorRemoto = new Jugador(nombreOponente, "Azul");
            System.out.println("👤 Jugador remoto creado: " + nombreOponente);
        }

        // Crear tableros
        Tablero tableroLocal = new Tablero(10);
        tableroLocal.setJugador(jugadorLocal);

        Tablero tableroRemoto = new Tablero(10);
        tableroRemoto.setJugador(jugadorRemoto);

        // Asignar tableros a jugadores
        if (jugadorLocal.getTableros() == null || jugadorLocal.getTableros().isEmpty()) {
            jugadorLocal.setTableros(new ArrayList<>());
            jugadorLocal.getTableros().add(tableroLocal);
        }

        if (jugadorRemoto.getTableros() == null || jugadorRemoto.getTableros().isEmpty()) {
            jugadorRemoto.setTableros(new ArrayList<>());
            jugadorRemoto.getTableros().add(tableroRemoto);
        }

        // Crear partida
        if (partida == null) {
            partida = new Partida();
            partida.agregarJugador(jugadorLocal);
            partida.agregarJugador(jugadorRemoto);
            partida.iniciar();
        }

        // Configurar turnos (cliente NO empieza)
        jugadorLocal.setTurno(false);
        jugadorRemoto.setTurno(true);
        miTurno = false;

        partidaIniciada = true;

        System.out.println("✅ Partida creada para cliente");
        System.out.println("   Turno inicial: " + (miTurno ? "Mío" : "Del oponente"));
    }

    private void procesarDisparoRecibido(String coordenada) {
        try {
            ResultadoDisparo resultado;
            if (soyServidor) {
                resultado = partida.disparar(jugadorRemoto, jugadorLocal, coordenada);
            } else {
                resultado = partida.disparar(jugadorRemoto, jugadorLocal, coordenada);
            }

            enviarMensaje("RESULTADO:" + coordenada + ":" + resultado.name());

            // Verificar si perdí
            Tablero miTablero = getTableroLocal();
            if (miTablero != null && miTablero.todasLasNavesHundidas()) {
                enviarMensaje("GANADOR");
                System.out.println("💀 ¡Has perdido! Todas tus naves fueron hundidas.");
            } else {
                miTurno = true;
                enviarMensaje("TURNO");
                System.out.println("Turno cambiado, ahora es mi turno");
            }

        } catch (Exception e) {
            System.err.println("Error procesando disparo recibido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public interface ConexionConfirmadaListener {

        void onConexionConfirmada();
    }

    private ConexionConfirmadaListener conexionListener;

    public void setConexionConfirmadaListener(ConexionConfirmadaListener listener) {
        this.conexionListener = listener;
    }

    private void notificarConexionConfirmada() {
        if (conexionListener != null) {
            conexionListener.onConexionConfirmada();
        }
    }

    private void notificarPartidaLista() {
        System.out.println("🔔 Notificando que la partida está lista para iniciar");
        if (conexionListener != null) {
            conexionListener.onConexionConfirmada();
        }
    }

    private Tablero getTableroLocal() {
        if (jugadorLocal == null || jugadorLocal.getTableros() == null || jugadorLocal.getTableros().isEmpty()) {
            return null;
        }
        return jugadorLocal.getTableros().get(0);
    }

    private Tablero getTableroRemoto() {
        if (jugadorRemoto == null || jugadorRemoto.getTableros() == null || jugadorRemoto.getTableros().isEmpty()) {
            return null;
        }
        return jugadorRemoto.getTableros().get(0);
    }

    // =============== MÉTODOS P2P ===============
    public void iniciarComoServidor(String nombre) {
        this.miNombre = nombre;

        if (jugadorLocal == null) {
            jugadorLocal = new Jugador(miNombre, "Azul");

            jugadorLocal.setTableros(new ArrayList<>()); 

            Tablero tableroLocal = new Tablero(10);
            tableroLocal.setJugador(jugadorLocal);
            jugadorLocal.getTableros().add(tableroLocal);  

            System.out.println("👑 Servidor creado con tablero");
        }

        this.soyServidor = true;
        this.miTurno = true;

        try {
            comunicacionService.startAsServer();
        } catch (Exception e) {
            System.err.println("Error al iniciar servidor: " + e.getMessage());
        }
    }

    public void conectarComoCliente(String serverId, String nombre) {
        this.miNombre = nombre;

        // ✅ CREAR JUGADOR PRIMERO
        if (jugadorLocal == null) {
            jugadorLocal = new Jugador(miNombre, "Rojo");
            jugadorLocal.setTableros(new ArrayList<>());
            Tablero tableroLocal = new Tablero(10);
            tableroLocal.setJugador(jugadorLocal);
            jugadorLocal.getTableros().add(tableroLocal);
            System.out.println("👤 Cliente creado con tablero");
        }

        this.soyServidor = false;
        this.miTurno = false;

        try {
            comunicacionService.connectToServer(serverId);
        } catch (Exception e) {
            System.err.println("Error al conectar: " + e.getMessage());
        }
    }

    public void realizarDisparoOnline(String coordenada) {
        if (!miTurno) {
            System.out.println("⏳ No es tu turno, espera...");
            return;
        }

        ResultadoDisparo resultado = dispararDesdeLocal(coordenada);

        if (resultado != null) {
            enviarMensaje("DISPARO:" + coordenada);
            miTurno = false;

            Tablero tableroRemoto = getTableroRemoto();
            if (tableroRemoto != null && tableroRemoto.todasLasNavesHundidas()) {
                System.out.println("🎉 ¡Has ganado! Hundiste todas las naves del oponente.");
                enviarMensaje("GANADOR");
            }
        }
    }

    private ResultadoDisparo dispararDesdeLocal(String coordenada) {
        try {
            if (soyServidor) {
                return dispararDesdeA(coordenada);
            } else {
                return dispararDesdeB(coordenada);
            }
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    private void iniciarPartidaOnline() {
        System.out.println("🔄 iniciarPartidaOnline() llamado - PERO NO creamos partida todavía");

        // ✅ SOLO crear jugadores básicos, NO la partida completa
        if (jugadorLocal == null) {
            if (miNombre != null && !miNombre.startsWith("Jugador_")) {
                jugadorLocal = new Jugador(miNombre, soyServidor ? "Azul" : "Rojo");
                System.out.println("👤 Jugador local básico creado: " + miNombre);
            }
        }

        if (jugadorRemoto == null && nombreOponente != null) {
            jugadorRemoto = new Jugador(nombreOponente, soyServidor ? "Rojo" : "Azul");
            System.out.println("👤 Jugador remoto básico creado: " + nombreOponente);
        }

        System.out.println("📝 Partida NO creada todavía - esperando colocación de naves");
    }

    public String getServerId() {
        if (comunicacionService == null) {
            System.err.println("Error: comunicacionService es null");
            return null;
        }
        try {
            return comunicacionService.getServerId();
        } catch (Exception e) {
            System.err.println("Error en getServerId: " + e.getMessage());
            return null;
        }
    }

    // =============== MÉTODOS ORIGINALES (modo local) ===============
    public ResultadoDisparo dispararDesdeA(String coordenada) {
        if (!jugadorLocal.isTurno()) {
            throw new IllegalStateException("No es el turno del Jugador A");
        }
        return partida.disparar(jugadorLocal, jugadorRemoto, coordenada);
    }

    public ResultadoDisparo dispararDesdeB(String coordenada) {
        if (!jugadorRemoto.isTurno()) {
            throw new IllegalStateException("No es el turno del Jugador B");
        }
        return partida.disparar(jugadorRemoto, jugadorLocal, coordenada);
    }

    public Tablero getTableroOponente(Jugador jugadorActual) {
        if (jugadorActual == jugadorLocal) {
            return jugadorRemoto.getTableros().get(0);
        } else {
            return jugadorLocal.getTableros().get(0);
        }
    }

    // =============== MÉTODOS AUXILIARES ===============
    private void enviarMensaje(String mensaje) {
        if (comunicacionService != null) {
            try {
                System.out.println("📤 Enviando mensaje: " + mensaje);
                comunicacionService.sendMessage(mensaje);
            } catch (Exception e) {
                System.err.println("Error enviando mensaje: " + e.getMessage());
            }
        } else {
            System.err.println("⚠️  No se puede enviar mensaje: comunicaciónService es null");
        }
    }

    private void mostrarResultadoDisparo(String coordenada, ResultadoDisparo resultado) {
        System.out.println("Resultado de disparo en " + coordenada + ": " + resultado);
    }

    // =============== NUEVOS MÉTODOS PARA COLOCACIÓN ===============
    public void finalizarColocacionJugador(List<Nave> navesColocadas) {
        this.colocacionCompletadaLocal = true;
        System.out.println("✅ " + miNombre + " completó su colocación");

        if (jugadorLocal == null) {
            jugadorLocal = new Jugador(miNombre, soyServidor ? "Azul" : "Rojo");
            System.out.println("👤 Jugador local creado: " + miNombre);
        }

        if (jugadorLocal.getTableros() == null || jugadorLocal.getTableros().isEmpty()) {
            Tablero tableroLocal = new Tablero(10);
            tableroLocal.setJugador(jugadorLocal);
            jugadorLocal.setTableros(new ArrayList<>());
            jugadorLocal.getTableros().add(tableroLocal);
            System.out.println("🎯 Tablero creado para jugador local");
        }

        Tablero tablero = jugadorLocal.getTableros().get(0);
        tablero.setNaves(navesColocadas);
        try {
            tablero.colocarNavesEnCasillas();
            System.out.println("🚢 " + navesColocadas.size() + " naves asignadas al jugador local");
        } catch (Exception e) {
            System.err.println("⚠️ Error al colocar naves: " + e.getMessage());
        }

        enviarMensaje("COLOCACION_COMPLETADA");
        verificarColocacionesCompletadas();
    }

    private void verificarColocacionesCompletadas() {
        System.out.println("🔍 Verificando colocaciones:");
        System.out.println("   Local (" + miNombre + "): " + colocacionCompletadaLocal);
        System.out.println("   Remota (" + nombreOponente + "): " + colocacionCompletadaRemota);

        if (colocacionCompletadaLocal && colocacionCompletadaRemota) {
            System.out.println("🎉 ¡Ambos jugadores completaron la colocación!");
            partidaListaParaIniciar = true;

            // ✅ AQUÍ es donde debemos crear la partida (solo una vez)
            if (!partidaIniciada) {
                crearPartidaOnlineCompleta();  // ← NUEVO MÉTODO
            } else {
                System.out.println("⚠️  Partida ya estaba iniciada, no se recrea");
            }

            // Enviar mensaje de sincronización para que AMBOS abran PanelJuego
            if (soyServidor) {
                enviarMensaje("INICIAR_PARTIDA");
                System.out.println("📤 Servidor: Enviando INICIAR_PARTIDA al cliente");
            }

            // Notificar a la vista que la partida está lista
            notificarPartidaLista();
        } else if (colocacionCompletadaLocal) {
            System.out.println("⏳ Esperando que " + nombreOponente + " complete su colocación...");
        } else if (colocacionCompletadaRemota) {
            System.out.println("⏳ Esperando que " + miNombre + " complete su colocación...");
        }
    }

    public void reiniciarFlagsColocacion() {
        colocacionCompletadaLocal = false;
        colocacionCompletadaRemota = false;
        partidaListaParaIniciar = false;
        System.out.println("🔄 Flags de colocación reiniciados");
    }

    // =============== GETTERS ===============
    public Jugador getJugadorLocal() {
        return jugadorLocal;
    }

    public Jugador getJugadorRemoto() {
        return jugadorRemoto;
    }

    public Jugador getJugadorA() {
        return jugadorLocal;
    }

    public Jugador getJugadorB() {
        return jugadorRemoto;
    }

    public Partida getPartida() {
        // ✅ SIEMPRE devuelve una partida (crea una si no existe)
        if (partida == null) {
            partida = new Partida();
            System.out.println("⚠️  Partida creada automáticamente en getPartida()");

            // Si ya tenemos jugadores, agrégales
            if (jugadorLocal != null && !partida.getJugadores().contains(jugadorLocal)) {
                partida.agregarJugador(jugadorLocal);
            }
            if (jugadorRemoto != null && !partida.getJugadores().contains(jugadorRemoto)) {
                partida.agregarJugador(jugadorRemoto);
            }

            // Si la partida está lista para iniciar, iníciala
            if (partidaListaParaIniciar && partida.getJugadores().size() >= 2) {
                partida.iniciar();
            }
        }
        return partida;
    }

    public boolean isMiTurno() {
        return miTurno;
    }

    public boolean isSoyServidor() {
        return soyServidor;
    }

    public String getMiNombre() {
        return miNombre;
    }

    public String getNombreOponente() {
        return nombreOponente;
    }

    public boolean isPartidaIniciada() {
        return partidaIniciada;
    }

    public boolean isPartidaListaParaIniciar() {
        return partidaListaParaIniciar;
    }

    public void desconectar() {
        if (comunicacionService != null) {
            comunicacionService.stop();
        }
    }
}
