package battleship.interfaces.controllers;

import battleship.domain.model.Casilla;
import battleship.domain.model.Tablero;
import battleship.application.ControlJuego;
import battleship.application.services.ComunicacionService;
import battleship.domain.model.Jugador;
import battleship.interfaces.views.PanelJuego;
import javax.swing.*;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionListener;

public class ControlVista implements ControlJuego.PanelJuegoListener {

    private final Map<String, JButton> botones = new HashMap<>();
    private final JPanel panelTablero;
    private final CeldaClickListener listener;
    private ControlJuego controlJuego;
    private JFrame ventanaActual;
    private JFrame menuView;
    private PartidaController partidaController;
    private ComunicacionService comunicacionService;
    private boolean servidorYaAbrioColocacion = false;
    private boolean panelColocacionAbierto = false;

    private final Map<String, ActionListener> listeners = new HashMap<>();

    // Estados de conexión
    private JDialog dialogoEspera;
    private Timer verificadorConexion;
    private boolean esperandoConexion = false;
    private boolean conexionConfirmada = false;
    private boolean clienteEsperandoConfirmacion = false;

    // Nuevos estados para colocación
    private boolean colocacionCompletadaLocal = false;

    public interface CeldaClickListener {

        void onCeldaClick(int fila, int col);
    }

    // Constructores
    public ControlVista(CeldaClickListener listener, JPanel panelTablero,
            ControlJuego controlJuego) {
        this.listener = listener;
        this.panelTablero = panelTablero;
        this.controlJuego = controlJuego;

        this.controlJuego.setPanelJuegoListener(this);

        configurarListenerConexion();
    }

    public ControlVista(ControlJuego controlJuego) {
        this.listener = null;
        this.panelTablero = null;
        this.controlJuego = controlJuego;
        this.partidaController = null;
        this.controlJuego.setPanelJuegoListener(this);

        configurarListenerConexion();
    }

    public ControlVista(CeldaClickListener listener, JPanel panelTablero,
            ControlJuego controlJuego, PartidaController partidaController) {
        this.listener = listener;
        this.panelTablero = panelTablero;
        this.controlJuego = controlJuego;
        this.partidaController = partidaController;
        this.controlJuego.setPanelJuegoListener(this);
        configurarListenerConexion();
    }

    @Override
    public void abrirPanelJuego(ControlJuego controlJuego, Jugador jugador) {
        System.out.println("🖼️  ControlVista.abrirPanelJuego() llamado - INICIO");
        System.out.println("   Desde: " + (controlJuego.isSoyServidor() ? "SERVIDOR" : "CLIENTE"));
        System.out.println("   Jugador: " + jugador.getNombre());
        System.out.println("   Partida lista? " + controlJuego.isPartidaListaParaIniciar());
        System.out.println("   servidorYaAbrioColocacion? " + servidorYaAbrioColocacion);
        System.out.println("   conexionConfirmada? " + conexionConfirmada);

        SwingUtilities.invokeLater(() -> {
            try {
                // ✅ LÓGICA MEJORADA:

                // 1. Si soy SERVIDOR y YA abrí PanelColocacion → IGNORAR
                if (controlJuego.isSoyServidor() && servidorYaAbrioColocacion) {
                    System.out.println("ℹ️  Servidor: Ya tengo PanelColocacion abierto, ignorando...");
                    return;
                }

                // 2. Si soy CLIENTE y NO tengo conexión confirmada → IGNORAR (esperar)
                if (!controlJuego.isSoyServidor() && !conexionConfirmada) {
                    System.out.println("⏳ Cliente: Esperando confirmación de conexión...");
                    return;
                }

                // 3. Si la partida NO está lista → ABRIR PANELCOLOCACION
                if (!controlJuego.isPartidaListaParaIniciar()) {
                    System.out.println("🚀 Abriendo PanelColocacion...");

                    // Marcar que el servidor ya abrió (si es servidor)
                    if (controlJuego.isSoyServidor()) {
                        servidorYaAbrioColocacion = true;
                    }

                    abrirPanelColocacion();

                } else {
                    // 4. Si la partida SÍ está lista → ABRIR PANELJUEGO
                    System.out.println("🎮 La partida SÍ está lista, abriendo PanelJuego...");

                    // Verificar que tenemos los jugadores necesarios
                    if (controlJuego.getJugadorLocal() == null || controlJuego.getJugadorRemoto() == null) {
                        System.err.println("❌ Error: Falta algún jugador");
                        JOptionPane.showMessageDialog(null,
                                "Error: No se pudieron cargar los jugadores.\nIntenta reconectar.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Crear PartidaController y PanelJuego
                    PartidaController partidaController = new PartidaController(controlJuego, jugador);
                    PanelJuego panelJuego = new PanelJuego(partidaController);

                    String titulo = "🚢 BATTLESHIP - " + jugador.getNombre()
                            + " vs " + controlJuego.getNombreOponente();
                    panelJuego.setTitle(titulo);
                    panelJuego.setSize(1400, 800);
                    panelJuego.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    panelJuego.setLocationRelativeTo(null);
                    panelJuego.setVisible(true);

                    System.out.println("✅ PanelJuego abierto exitosamente");
                }

            } catch (Exception e) {
                System.err.println("❌ Error en abrirPanelJuego: " + e.getMessage());
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // =============== CONFIGURAR LISTENER DE CONEXIÓN Y COLOCACIÓN ===============
    private void configurarListenerConexion() {
        if (controlJuego != null) {
            controlJuego.setConexionConfirmadaListener(new ControlJuego.ConexionConfirmadaListener() {
                @Override
                public void onConexionConfirmada() {
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("🔔 onConexionConfirmada() - Jugador: " + controlJuego.getMiNombre());
                        System.out.println("   Soy servidor? " + controlJuego.isSoyServidor());
                        System.out.println("   Partida lista? " + controlJuego.isPartidaListaParaIniciar());
                        System.out.println("   Colocación local? " + colocacionCompletadaLocal);

                        if (!controlJuego.isSoyServidor() && !conexionConfirmada) {
                            conexionConfirmada = true;
                            clienteEsperandoConfirmacion = false;
                            System.out.println("✅ Cliente: Confirmación INICIAL recibida");
                            verificarRedireccion();  // Esto abre PanelColocacion (CORRECTO)
                            return;
                        }

                        System.out.println("📝 onConexionConfirmada() - Ignorando llamada adicional");
                    });
                }
            });
        }
    }

    private void verificarSiPartidaLista() {
        System.out.println("🔍 Verificando condiciones para iniciar partida...");

        boolean puedeAbrir = false;
        String razon = "";

        if (controlJuego.isSoyServidor()) {
            if (colocacionCompletadaLocal && conexionConfirmada) {
                puedeAbrir = true;
                razon = "Servidor: Ambos completaron colocación";
            } else {
                razon = "Servidor: Aún no completamos colocación o sin conexión";
            }
        } else {
            if (colocacionCompletadaLocal && conexionConfirmada) {
                puedeAbrir = true;
                razon = "Cliente: Completamos colocación y estamos conectados";
            } else {
                razon = "Cliente: Esperando completar colocación o conexión";
            }
        }

        System.out.println("🔍 " + razon + " - Puede abrir: " + puedeAbrir);

        if (puedeAbrir) {
            System.out.println("🎯 Condiciones cumplidas, pero esperando INICIAR_PARTIDA...");
        }
    }

    // =============== SETTERS ===============
    public void setMenuView(JFrame menuView) {
        this.menuView = menuView;
        if (menuView != null && ventanaActual == null) {
            this.ventanaActual = menuView;
        }
    }

    public void setVentanaActual(JFrame ventana) {
        this.ventanaActual = ventana;
    }

    // =============== NUEVO: NOTIFICAR COLOCACIÓN COMPLETADA ===============
    public void notificarColocacionCompletada() {
        this.colocacionCompletadaLocal = true;
        System.out.println("ControlVista: Colocación local marcada como completada");

        System.out.println("Notificando al oponente que completamos colocación...");
    }

    // =============== MÉTODOS P2P ===============
    public void crearPartida() {
        if (controlJuego == null) {
            mostrarError("Error", "ControlJuego no inicializado");
            return;
        }

        String nombre = JOptionPane.showInputDialog(
                ventanaActual,
                "Introduce tu nombre:",
                "Crear partida",
                JOptionPane.PLAIN_MESSAGE
        );

        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                controlJuego.iniciarComoServidor(nombre.trim());

                String serverId = controlJuego.getServerId();
                if (serverId != null) {
                    mostrarMensajeEsperaNoModal(
                            "Partida creada exitosamente.\n"
                            + "ID para compartir: " + serverId + "\n\n"
                            + "⚠️ ESPERANDO que el oponente se conecte...\n"
                            + "Pasa este ID al otro jugador:\n"
                            + "🔸 " + serverId + "\n\n"
                            + "Mientras tanto, ya puedes colocar tus naves.",
                            "Esperando oponente..."
                    );

                    esperandoConexion = true;

                

                    // No necesitamos verificador de conexión para el servidor
                    // porque ya está en PanelColocacion
                }
            } catch (Exception e) {
                mostrarError("Error al crear partida", e.getMessage());
            }
        }
    }

    public void unirseAPartida(String serverId) {
        if (controlJuego == null) {
            mostrarError("Error", "ControlJuego no inicializado");
            return;
        }

        String nombre = JOptionPane.showInputDialog(
                ventanaActual,
                "Introduce tu nombre:",
                "Unirse a partida",
                JOptionPane.PLAIN_MESSAGE
        );

        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                controlJuego.conectarComoCliente(serverId.trim(), nombre.trim());

                mostrarMensajeEsperaNoModal(
                        "Conectando a: " + serverId + "\n\n"
                        + "Esperando confirmación del servidor...\n"
                        + "Cuando la conexión sea exitosa,\n"
                        + "ambos serán redirigidos automáticamente.",
                        "Conectando..."
                );

                esperandoConexion = true;
                clienteEsperandoConfirmacion = true;
                iniciarVerificadorConexion();

            } catch (Exception e) {
                mostrarError("Error al conectar", e.getMessage());
            }
        }
    }

    // =============== MÉTODO PARA MOSTRAR MENSAJE DE ESPERA NO MODAL ===============
    private void mostrarMensajeEsperaNoModal(String mensaje, String titulo) {
        SwingUtilities.invokeLater(() -> {
            if (dialogoEspera != null && dialogoEspera.isVisible()) {
                dialogoEspera.dispose();
            }

            JOptionPane optionPane = new JOptionPane(
                    mensaje,
                    JOptionPane.INFORMATION_MESSAGE
            );

            dialogoEspera = optionPane.createDialog(ventanaActual, titulo);
            dialogoEspera.setModal(false);

            if (ventanaActual != null) {
                java.awt.Point loc = ventanaActual.getLocation();
                dialogoEspera.setLocation(loc.x + 100, loc.y + 100);
            }

            dialogoEspera.setAlwaysOnTop(true);

            JButton cancelarButton = new JButton("Cancelar");
            cancelarButton.addActionListener(e -> {
                dialogoEspera.dispose();
                cancelarEspera();
            });

            optionPane.setOptions(new Object[]{cancelarButton});
            dialogoEspera.setVisible(true);
        });
    }

    // =============== CANCELAR ESPERA ===============
    private void cancelarEspera() {
        esperandoConexion = false;
        conexionConfirmada = false;
        clienteEsperandoConfirmacion = false;
        colocacionCompletadaLocal = false;

        if (verificadorConexion != null) {
            verificadorConexion.stop();
        }

        if (controlJuego != null) {
            controlJuego.desconectar();
        }

        System.out.println("Espera cancelada por el usuario");
    }

    // =============== CERRAR DIÁLOGO DE ESPERA ===============
    private void cerrarDialogoEspera() {
        SwingUtilities.invokeLater(() -> {
            if (dialogoEspera != null && dialogoEspera.isVisible()) {
                dialogoEspera.dispose();
                dialogoEspera = null;
            }
        });
    }

    // =============== VERIFICADOR DE CONEXIÓN ===============
    private void iniciarVerificadorConexion() {
        if (verificadorConexion != null) {
            verificadorConexion.stop();
        }

        verificadorConexion = new Timer(1000, e -> {
            verificarEstadoConexion();
        });
        verificadorConexion.start();
    }

    private void verificarEstadoConexion() {
        if (!esperandoConexion) {
            return;
        }

        // Para SERVIDOR: verificar si la partida está iniciada
        if (controlJuego.isSoyServidor() && controlJuego.isPartidaIniciada()) {
            if (!conexionConfirmada) {
                conexionConfirmada = true;
                System.out.println("✅ Servidor: Cliente conectado, redirigiendo...");

                // Redirigir después de un breve retraso
                Timer redireccionTimer = new Timer(2000, ev -> {
                    verificarRedireccion();
                });
                redireccionTimer.setRepeats(false);
                redireccionTimer.start();
            }
        } // Para CLIENTE: verificar si recibió confirmación
        else if (!controlJuego.isSoyServidor()) {
            // El cliente espera la confirmación del servidor
            // que llega a través del listener configurado en configurarListenerConexion()
            if (conexionConfirmada && clienteEsperandoConfirmacion) {
                clienteEsperandoConfirmacion = false;
                System.out.println("✅ Cliente: Confirmación recibida, redirigiendo...");
                verificarRedireccion();
            }
        }
    }

    // =============== VERIFICAR SI ES MOMENTO DE REDIRIGIR ===============
    private void verificarRedireccion() {
        if (!esperandoConexion || !conexionConfirmada) {
            return;
        }

        // Detener el verificador
        if (verificadorConexion != null) {
            verificadorConexion.stop();
        }

        SwingUtilities.invokeLater(() -> {
            cerrarDialogoEspera();

            // ✅ VERIFICAR si YA estamos en PanelColocacion
            if (ventanaActual != null && ventanaActual.getTitle() != null
                    && ventanaActual.getTitle().contains("Colocación")) {
                System.out.println("ℹ️  Ya estamos en PanelColocacion, no redirigir");
                return;
            }

            // ✅ VERIFICAR si YA abrimos PanelColocacion
            if (panelColocacionAbierto) {
                System.out.println("ℹ️  PanelColocacion ya abierto, no redirigir");
                return;
            }

            System.out.println("🚀 Redirigiendo a PanelColocacion...");
            abrirPanelColocacion();
        });
    }

    // =============== MOSTRAR CONFIRMACIÓN FINAL ===============
    private void mostrarMensajeConfirmacionFinal(String mensaje) {
        JOptionPane.showMessageDialog(
                ventanaActual,
                mensaje,
                "¡Conexión exitosa!",
                JOptionPane.INFORMATION_MESSAGE
        );

        abrirPanelColocacion();
    }

    // =============== MÉTODO PARA ABRIR PANELCOLOCACION ===============
    private void abrirPanelColocacion() {
        try {
            if (menuView != null) {
                menuView.setVisible(false);
                menuView.dispose();
            }

            // Reiniciar estados para nueva colocación
            colocacionCompletadaLocal = false;

            Class<?> panelClass = Class.forName("battleship.interfaces.views.PanelColocacion");

            JFrame panelColocacion = null;

            try {
                // Intentar constructor con ControlVista y ControlJuego
                panelColocacion = (JFrame) panelClass.getConstructor(ControlVista.class, ControlJuego.class)
                        .newInstance(this, controlJuego);
            } catch (NoSuchMethodException e1) {
                try {
                    // Intentar constructor solo con ControlJuego
                    panelColocacion = (JFrame) panelClass.getConstructor(ControlJuego.class)
                            .newInstance(controlJuego);
                } catch (NoSuchMethodException e2) {
                    try {
                        // Intentar constructor vacío
                        panelColocacion = (JFrame) panelClass.getConstructor().newInstance();
                    } catch (NoSuchMethodException e3) {
                        throw new RuntimeException("No se encontró un constructor adecuado para PanelColocacion");
                    }
                }
            }

            if (panelColocacion != null) {
                String titulo = "🚢 BATTLESHIP - Colocación de Naves - "
                        + controlJuego.getMiNombre()
                        + (controlJuego.isSoyServidor() ? " (Servidor)" : " (Cliente)");
                panelColocacion.setTitle(titulo);
                panelColocacion.setSize(1400, 800);
                panelColocacion.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                panelColocacion.setLocationRelativeTo(null);
                panelColocacion.setVisible(true);

                this.ventanaActual = panelColocacion;

                System.out.println("✅ Pantalla de colocación abierta para: "
                        + controlJuego.getMiNombre()
                        + (controlJuego.isSoyServidor() ? " (Servidor)" : " (Cliente)"));
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: Clase PanelColocacion no encontrada");

            JOptionPane.showMessageDialog(ventanaActual,
                    "Error: No se pudo encontrar la pantalla de colocación.\n"
                    + "Clase no encontrada: battleship.interfaces.views.PanelColocacion",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            if (menuView != null) {
                menuView.setVisible(true);
            }

        } catch (Exception e) {
            System.err.println("❌ Error al abrir PanelColocacion: " + e.getMessage());

            JOptionPane.showMessageDialog(ventanaActual,
                    "Error al abrir la pantalla de colocación:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            if (menuView != null) {
                menuView.setVisible(true);
            }
        }
    }

    // =============== MÉTODO PARA ABRIR PANELJUEGO ===============
    public void abrirPanelJuego() {
        try {
            System.out.println("🎮 Intentando abrir PanelJuego para: " + controlJuego.getMiNombre());
            System.out.println("   Soy servidor? " + controlJuego.isSoyServidor());
            System.out.println("   Partida iniciada? " + controlJuego.isPartidaIniciada());

            // Verificar que el controlJuego tenga jugador local
            Jugador jugadorLocal = controlJuego.getJugadorLocal();
            if (jugadorLocal == null) {
                System.err.println("❌ Error: jugadorLocal es null en ControlJuego");

                // Intentar crear jugador si no existe
                System.out.println("⚠️ Creando jugador local manualmente...");
                jugadorLocal = new Jugador(controlJuego.getMiNombre(),
                        controlJuego.isSoyServidor() ? "Azul" : "Rojo");

                // También necesitamos crear jugador remoto para que PartidaController funcione
                if (controlJuego.getJugadorRemoto() == null) {
                    Jugador jugadorRemoto = new Jugador(controlJuego.getNombreOponente(),
                            controlJuego.isSoyServidor() ? "Rojo" : "Azul");
                    // Aquí necesitarías un método para asignar jugador remoto
                    System.out.println("👥 Jugador remoto creado: " + jugadorRemoto.getNombre());
                }

                // Intentar forzar la creación de partida si no existe
                if (controlJuego.getPartida() == null) {
                    System.out.println("⚠️ Creando partida manualmente...");
                    // Necesitarías acceso a un método para crear partida
                }
            }

            // Verificar que tenemos todos los componentes necesarios
            if (controlJuego.getPartida() == null) {
                System.err.println("❌ Error: No hay partida creada");
                mostrarError("Error", "No se pudo crear la partida. Intenta reconectar.");
                return;
            }

            // Crear PartidaController
            System.out.println("🎯 Creando PartidaController...");
            PartidaController partidaController = new PartidaController(controlJuego, jugadorLocal);

            // Crear PanelJuego
            System.out.println("🖼️ Creando PanelJuego...");
            PanelJuego panelJuego = new PanelJuego(partidaController);

            // Configurar ventana
            String titulo = "🚢 BATTLESHIP - "
                    + controlJuego.getMiNombre() + " (Tú) vs "
                    + controlJuego.getNombreOponente();
            panelJuego.setTitle(titulo);
            panelJuego.setSize(1400, 800);
            panelJuego.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            panelJuego.setLocationRelativeTo(null);
            panelJuego.setVisible(true);

            // Actualizar ventana actual
            this.ventanaActual = panelJuego;

            System.out.println("✅ PanelJuego abierto exitosamente para: " + controlJuego.getMiNombre());
            System.out.println("   Título: " + titulo);

        } catch (Exception e) {
            System.err.println("❌ Error crítico al abrir PanelJuego: " + e.getMessage());
            e.printStackTrace();

            // Mostrar mensaje de error detallado
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Error al abrir la pantalla de juego:\n\n");
            errorMsg.append(e.getMessage()).append("\n\n");
            errorMsg.append("Causa: ").append(e.getCause() != null ? e.getCause().getMessage() : "Desconocida").append("\n\n");
            errorMsg.append("Solución:\n");
            errorMsg.append("1. Verifica que ambos jugadores estén conectados\n");
            errorMsg.append("2. Ambos deben completar la colocación de naves\n");
            errorMsg.append("3. Intenta reiniciar la aplicación");

            JOptionPane.showMessageDialog(ventanaActual,
                    errorMsg.toString(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =============== MÉTODOS ORIGINALES ===============
    public void mostrarPantallaJuego(JFrame pantallaJuego) {
        if (menuView != null) {
            menuView.setVisible(false);
        }
        if (pantallaJuego != null) {
            pantallaJuego.setVisible(true);
        }
    }

    public void volverAlMenu() {
        cancelarEspera();

        if (menuView != null) {
            menuView.setVisible(true);
        }

        cerrarDialogoEspera();
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    ventanaActual,
                    mensaje,
                    titulo,
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private void mostrarError(String titulo, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    ventanaActual,
                    mensaje,
                    titulo,
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    // =============== MÉTODOS DE TABLERO ===============
    public void generarTablero(Tablero tablero) {
        if (panelTablero == null) {
            System.err.println("Error: panelTablero es null");
            return;
        }

        int n = tablero.getMedidas();
        int buttonSize = 60;

        panelTablero.removeAll();
        botones.clear();
        listeners.clear();

        ImageIcon iconAgua = cargarIcono("/imagenes/CasillaAgua.png");

        panelTablero.setLayout(null);

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coordKey = fila + "," + col;

                JButton btn = new JButton();
                btn.setBounds(col * buttonSize, fila * buttonSize, buttonSize, buttonSize);
                btn.setIcon(iconAgua);

                btn.setFocusPainted(false);
                btn.setContentAreaFilled(true);
                btn.setBorderPainted(true);

                int ff = fila, cc = col;

                ActionListener actionListener = e -> {
                    if (listener != null) {
                        listener.onCeldaClick(ff, cc);
                    }
                };
                btn.addActionListener(actionListener);
                listeners.put(coordKey, actionListener);

                panelTablero.add(btn);
                botones.put(coordKey, btn);
            }
        }

        panelTablero.setPreferredSize(new Dimension(n * buttonSize, n * buttonSize));
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    public void actualizarBotones(Tablero tablero) {
        if (panelTablero == null) {
            return;
        }

        ImageIcon iconAgua = cargarIcono("/imagenes/CasillaAgua.png");
        ImageIcon iconImpacto = cargarIcono("/imagenes/DisparoAcertado.png");
        ImageIcon iconFallo = cargarIcono("/imagenes/DisparoFallido.png");

        for (int fila = 0; fila < tablero.getMedidas(); fila++) {
            for (int col = 0; col < tablero.getMedidas(); col++) {
                Casilla c = tablero.getCasilla(fila, col);
                String coordKey = fila + "," + col;
                JButton btn = botones.get(coordKey);

                if (btn == null) {
                    continue;
                }

                String estado = c.obtenerEstado();

                switch (estado) {
                    case "IMPACTO":
                        btn.setIcon(iconImpacto);
                        eliminarListener(coordKey);
                        btn.setEnabled(false);
                        break;

                    case "AGUA":
                        btn.setIcon(iconFallo);
                        eliminarListener(coordKey);
                        btn.setEnabled(false);
                        break;

                    default:
                        btn.setIcon(iconAgua);
                        restaurarListener(coordKey, fila, col);
                        btn.setEnabled(true);
                        break;
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            panelTablero.revalidate();
            panelTablero.repaint();
        });
    }

    private void eliminarListener(String coordKey) {
        ActionListener listener = listeners.get(coordKey);
        if (listener != null) {
            JButton btn = botones.get(coordKey);
            if (btn != null) {
                btn.removeActionListener(listener);
            }
            listeners.remove(coordKey);
        }
    }

    private void restaurarListener(String coordKey, int fila, int col) {
        if (listeners.get(coordKey) == null) {
            int ff = fila, cc = col;
            ActionListener newListener = e -> {
                if (this.listener != null) {
                    this.listener.onCeldaClick(ff, cc);
                }
            };

            JButton btn = botones.get(coordKey);
            if (btn != null) {
                btn.addActionListener(newListener);
                listeners.put(coordKey, newListener);
            }
        }
    }

    private ImageIcon cargarIcono(String ruta) {
        try {
            return new ImageIcon(getClass().getResource(ruta));
        } catch (Exception e) {
            System.err.println("Error cargando icono: " + ruta);
            return new ImageIcon(); // Icono vacío
        }
    }

    // =============== GETTERS ===============
    public ControlJuego getControlJuego() {
        return controlJuego;
    }

    public boolean isModoOnline() {
        return controlJuego != null && controlJuego.isPartidaIniciada();
    }

    public boolean isColocacionCompletadaLocal() {
        return colocacionCompletadaLocal;
    }
}
