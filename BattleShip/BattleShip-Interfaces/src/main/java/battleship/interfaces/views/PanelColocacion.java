package battleship.interfaces.views;

import battleship.interfaces.controllers.ControlVista;
import battleship.application.ControlJuego;
import battleship.domain.model.Tablero;
import battleship.domain.model.Jugador;
import battleship.domain.model.Nave;
import battleship.domain.enums.TipoNave;
import battleship.domain.enums.EstadoNave;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PanelColocacion extends javax.swing.JFrame {

    private ControlJuego controlJuego;
    private Tablero tablero;
    private JButton[][] botonesTablero;
    private int tamañoTablero = 10;
    private int tamañoCelda = 60;

    private TipoNave naveSeleccionada = null;
    private boolean orientacionHorizontal = true;
    private List<Nave> navesColocadas = new ArrayList<>();
    private boolean todasNavesColocadas = false;

    // Para modo eliminación
    private boolean modoEliminacion = false;
    private Nave naveAEliminar = null;

    // Para manejo de listeners
    private final Map<String, MouseListener> listenersMouse = new HashMap<>();
    private JButton btnListo;
    private JLabel lblEstado;
    private JPanel panelNavesContainer;
    private JButton btnEliminar;
    private JButton btnCancelarEliminacion;

    // Contadores de naves colocadas por tipo - NUEVA DISTRIBUCIÓN
    private Map<TipoNave, Integer> contadorNaves = new HashMap<>();

    // Configuración de naves - NUEVA DISTRIBUCIÓN
    private final Map<TipoNave, Integer> maxNavesPorTipo = new HashMap<>();
    private final Map<TipoNave, Integer> tamañoPorTipo = new HashMap<>();
    private final int totalNavesRequeridas = 11; // 2 + 2 + 4 + 3 = 11

    /**
     * Constructor principal
     */
    public PanelColocacion(ControlJuego controlJuego) {
        this.controlJuego = controlJuego;

        // Inicializar configuración de naves
        inicializarConfiguracionNaves();

        // Obtener tablero
        if (controlJuego != null && controlJuego.getJugadorLocal() != null) {
            Jugador jugador = controlJuego.getJugadorLocal();
            if (jugador.getTableros() != null && !jugador.getTableros().isEmpty()) {
                this.tablero = jugador.getTableros().get(0);
            }
        }

        // Si no hay tablero, crear uno temporal
        if (this.tablero == null) {
            this.tablero = new Tablero(10);
            System.out.println("⚠️ Usando tablero temporal");
        }

        initComponents();
        configurarInterfaz();

        // Configurar título
        String titulo = "🚢 BATTLESHIP - Colocación de Naves";
        if (controlJuego != null) {
            titulo += " - " + controlJuego.getMiNombre();
            if (controlJuego.isSoyServidor()) {
                titulo += " (Servidor)";
            } else {
                titulo += " (Cliente)";
            }
        }
        setTitle(titulo);

        setSize(1400, 800);
        setLocationRelativeTo(null);
    }

    /**
     * Constructor sin parámetros para compatibilidad
     */
    public PanelColocacion() {
        this(null);
    }

    /**
     * Inicializar configuración de naves - NUEVA DISTRIBUCIÓN
     */
    private void inicializarConfiguracionNaves() {
        // Configurar máxima cantidad por tipo
        maxNavesPorTipo.put(TipoNave.PORTA_AVIONES, 2);   // 2 Porta aviones
        maxNavesPorTipo.put(TipoNave.CRUCERO, 2);         // 2 Cruceros
        maxNavesPorTipo.put(TipoNave.SUBMARINO, 4);       // 4 Submarinos
        maxNavesPorTipo.put(TipoNave.BARCO, 3);           // 3 Barcos

        // Configurar tamaño por tipo
        tamañoPorTipo.put(TipoNave.PORTA_AVIONES, 4);     // 4 casillas
        tamañoPorTipo.put(TipoNave.CRUCERO, 3);           // 3 casillas
        tamañoPorTipo.put(TipoNave.SUBMARINO, 2);         // 2 casillas
        tamañoPorTipo.put(TipoNave.BARCO, 1);             // 1 casilla

        // Inicializar contadores en 0
        for (TipoNave tipo : TipoNave.values()) {
            contadorNaves.put(tipo, 0);
        }
    }

    /**
     * Obtener tamaño de una nave según su tipo
     */
    private int obtenerTamañoNave(TipoNave tipo) {
        return tamañoPorTipo.getOrDefault(tipo, 1);
    }

    /**
     * Obtener cantidad máxima de un tipo de nave
     */
    private int obtenerMaximoPorTipo(TipoNave tipo) {
        return maxNavesPorTipo.getOrDefault(tipo, 0);
    }

    /**
     * Configurar interfaz gráfica
     */
    private void configurarInterfaz() {
        // Cambiar layout del content pane a null para posicionamiento absoluto
        getContentPane().setLayout(null);

        // Asegurar que el fondo esté en la posición correcta
        jLabelFondo.setBounds(0, 0, 1400, 800);

        // Asegurar que el panel del tablero esté en la posición correcta
        panelTablero.setBounds(400, 100, 600, 600);
        panelTablero.setLayout(null);

        // Generar tablero
        generarTableroColocacion();

        // Añadir componentes de control UNA SOLA VEZ
        añadirControlesUnaVez();
    }

    /**
     * Añadir controles de interfaz UNA SOLA VEZ - ACTUALIZADO CON NUEVA
     * DISTRIBUCIÓN
     */
    private void añadirControlesUnaVez() {
        // Verificar si ya existen controles
        boolean controlesYaExisten = false;
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel && comp.getBounds().x == 50 && comp.getBounds().y == 100) {
                controlesYaExisten = true;
                break;
            }
        }

        if (controlesYaExisten) {
            return;
        }

        // Panel para naves disponibles - 4 tipos de naves + botón eliminar = 5 filas
        panelNavesContainer = new JPanel();
        panelNavesContainer.setLayout(new GridLayout(6, 1, 5, 5)); // 6 filas (4 naves + 1 separador + 1 botón eliminar)
        panelNavesContainer.setBounds(50, 100, 200, 400);
        panelNavesContainer.setOpaque(false);
        getContentPane().add(panelNavesContainer);

        // Botones de naves - SOLO 4 BOTONES (uno por tipo) con contadores dinámicos
        // Portaaviones (2 disponibles, 4 casillas)
        JButton btnPortaaviones = crearBotonNave(TipoNave.PORTA_AVIONES,
                "Portaaviones (4) [" + contadorNaves.get(TipoNave.PORTA_AVIONES) + "/2]");
        panelNavesContainer.add(btnPortaaviones);

        // Cruceros (2 disponibles, 3 casillas)
        JButton btnCrucero = crearBotonNave(TipoNave.CRUCERO,
                "Crucero (3) [" + contadorNaves.get(TipoNave.CRUCERO) + "/2]");
        panelNavesContainer.add(btnCrucero);

        // Submarinos (4 disponibles, 2 casillas)
        JButton btnSubmarino = crearBotonNave(TipoNave.SUBMARINO,
                "Submarino (2) [" + contadorNaves.get(TipoNave.SUBMARINO) + "/4]");
        panelNavesContainer.add(btnSubmarino);

        // Barcos (3 disponibles, 1 casilla)
        JButton btnBarco = crearBotonNave(TipoNave.BARCO,
                "Barco (1) [" + contadorNaves.get(TipoNave.BARCO) + "/3]");
        panelNavesContainer.add(btnBarco);

        // Separador
        panelNavesContainer.add(new JLabel(""));

        // Botón eliminar nave
        btnEliminar = new JButton("🗑️ Eliminar Nave");
        btnEliminar.setFont(new Font("Arial", Font.BOLD, 12));
        btnEliminar.setBackground(Color.ORANGE);
        btnEliminar.setForeground(Color.BLACK);
        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activarModoEliminacion();
            }
        });
        panelNavesContainer.add(btnEliminar);

        // Botón cancelar eliminación (inicialmente invisible)
        btnCancelarEliminacion = new JButton("✖️ Cancelar Eliminación");
        btnCancelarEliminacion.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelarEliminacion.setBackground(Color.RED);
        btnCancelarEliminacion.setForeground(Color.WHITE);
        btnCancelarEliminacion.setBounds(50, 510, 200, 40);
        btnCancelarEliminacion.setVisible(false);
        btnCancelarEliminacion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                desactivarModoEliminacion();
            }
        });
        getContentPane().add(btnCancelarEliminacion);

        // Etiqueta instrucciones
        JLabel lblInstrucciones = new JLabel("Selecciona una nave y haz clic en el tablero");
        lblInstrucciones.setBounds(50, 50, 400, 30);
        lblInstrucciones.setFont(new Font("Arial", Font.BOLD, 16));
        lblInstrucciones.setForeground(Color.WHITE);
        getContentPane().add(lblInstrucciones);

        // Botón rotar
        JButton btnRotar = new JButton("Rotar (R)");
        btnRotar.setBounds(50, 560, 120, 40);
        btnRotar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotarOrientacion();
            }
        });
        btnRotar.setFont(new Font("Arial", Font.BOLD, 14));
        getContentPane().add(btnRotar);

        // Botón listo
        btnListo = new JButton("✅ LISTO");
        btnListo.setBounds(180, 560, 120, 40);
        btnListo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizarColocacion();
            }
        });
        btnListo.setFont(new Font("Arial", Font.BOLD, 14));
        btnListo.setBackground(new Color(0, 180, 0));
        btnListo.setForeground(Color.WHITE);
        btnListo.setEnabled(false);
        getContentPane().add(btnListo);

        // Etiqueta estado - ACTUALIZADO para 11 naves
        lblEstado = new JLabel("Naves colocadas: 0/11");
        lblEstado.setBounds(50, 610, 300, 30);
        lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEstado.setForeground(Color.YELLOW);
        getContentPane().add(lblEstado);

        // Asegurar que el fondo esté detrás de todo
        getContentPane().setComponentZOrder(jLabelFondo, getContentPane().getComponentCount() - 1);
    }

    /**
     * Crear botón para una nave con contador actualizado
     */
    private JButton crearBotonNave(final TipoNave tipo, final String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 12));

        Color color = obtenerColorNave(tipo);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);

        // Deshabilitar si ya se colocó el máximo de este tipo
        int colocadas = contadorNaves.get(tipo);
        int maximo = obtenerMaximoPorTipo(tipo);

        if (colocadas >= maximo) {
            btn.setEnabled(false);
            btn.setBackground(Color.GRAY);
        }

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Verificar cuántas ya se colocaron
                int colocadasActual = contadorNaves.get(tipo);
                int maximoActual = obtenerMaximoPorTipo(tipo);

                if (colocadasActual < maximoActual) {
                    naveSeleccionada = tipo;
                    System.out.println("Nave seleccionada: " + tipo + " (Tamaño: " + obtenerTamañoNave(tipo) + ")");

                    // Deshabilitar este botón temporalmente
                    btn.setEnabled(false);

                    // Actualizar otros botones si es necesario
                    actualizarBotonesNaves();
                } else {
                    System.out.println("Ya se colocaron todas las naves de tipo: " + tipo);
                    btn.setEnabled(false);
                    btn.setBackground(Color.GRAY);
                }
            }
        });

        return btn;
    }

    private void reiniciarSeleccionNave() {
        naveSeleccionada = null;

        // Rehabilitar botones según contadores actuales
        Component[] components = panelNavesContainer.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String text = btn.getText();

                // Verificar si es un botón de nave (no el botón eliminar)
                if (!text.contains("Eliminar")) {
                    for (TipoNave tipo : TipoNave.values()) {
                        if (text.contains(obtenerNombreTipo(tipo))) {
                            int colocadas = contadorNaves.get(tipo);
                            int maximo = obtenerMaximoPorTipo(tipo);

                            if (colocadas < maximo) {
                                btn.setEnabled(true);
                                btn.setBackground(obtenerColorNave(tipo));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Obtener color para un tipo de nave
     */
    private Color obtenerColorNave(TipoNave tipo) {
        switch (tipo) {
            case PORTA_AVIONES:
                return Color.RED;
            case CRUCERO:
                return Color.GREEN.darker();
            case SUBMARINO:
                return Color.BLUE;
            case BARCO:
                return Color.MAGENTA.darker();
            default:
                return Color.GRAY;
        }
    }

    /**
     * Actualizar botones de naves con contadores actualizados
     */
    private void actualizarBotonesNaves() {
        Component[] components = panelNavesContainer.getComponents();

        // Mapa para relacionar tipos de nave con sus botones
        Map<TipoNave, JButton> botonesPorTipo = new HashMap<>();

        // Primero identificar qué botón corresponde a qué tipo
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String text = btn.getText();

                // Buscar qué tipo de nave es este botón
                for (TipoNave tipo : TipoNave.values()) {
                    String nombreTipo = obtenerNombreTipo(tipo);
                    if (text.contains(nombreTipo)) {
                        botonesPorTipo.put(tipo, btn);
                        break;
                    }
                }
            }
        }

        // Ahora actualizar cada botón según su tipo
        for (Map.Entry<TipoNave, JButton> entry : botonesPorTipo.entrySet()) {
            TipoNave tipo = entry.getKey();
            JButton btn = entry.getValue();

            int colocadas = contadorNaves.get(tipo);
            int maximo = obtenerMaximoPorTipo(tipo);

            // Actualizar texto del contador
            String nombreNave = obtenerNombreTipo(tipo);
            String tamaño = String.valueOf(obtenerTamañoNave(tipo));
            String nuevoTexto = nombreNave + " (" + tamaño + ") [" + colocadas + "/" + maximo + "]";
            btn.setText(nuevoTexto);

            // Habilitar/deshabilitar según contador
            if (colocadas >= maximo) {
                btn.setEnabled(false);
                btn.setBackground(Color.GRAY);
            } else if (tipo != naveSeleccionada) {
                btn.setEnabled(true);
                btn.setBackground(obtenerColorNave(tipo));
            }
        }
    }

    /**
     * Obtener nombre amigable del tipo
     */
    private String obtenerNombreTipo(TipoNave tipo) {
        switch (tipo) {
            case PORTA_AVIONES:
                return "Portaaviones";
            case CRUCERO:
                return "Crucero";
            case SUBMARINO:
                return "Submarino";
            case BARCO:
                return "Barco";
            default:
                return tipo.toString();
        }
    }

    /**
     * Generar el tablero visual
     */
    private void generarTableroColocacion() {
        panelTablero.removeAll();
        botonesTablero = new JButton[tamañoTablero][tamañoTablero];

        int buttonSize = 60;

        panelTablero.setLayout(null);

        // Cargar icono de agua
        ImageIcon iconAgua = cargarIcono("/imagenes/CasillaAgua.png");
        ImageIcon iconAguaFinal = iconAgua != null ? iconAgua : crearIconoColor(new Color(173, 216, 230), 60, 60);

        for (int fila = 0; fila < tamañoTablero; fila++) {
            for (int col = 0; col < tamañoTablero; col++) {
                String coordKey = fila + "," + col;

                JButton btn = new JButton();
                btn.setBounds(col * buttonSize, fila * buttonSize, buttonSize, buttonSize);

                // Usar icono de agua desde el principio
                btn.setIcon(iconAguaFinal);
                btn.setBackground(new Color(173, 216, 230));
                btn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setFocusPainted(false);
                btn.setBorderPainted(true);

                // Crear variables finales para usar en la clase interna
                final int filaFinal = fila;
                final int colFinal = col;

                // Crear MouseListener para esta celda
                MouseListener mouseListener = new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (modoEliminacion) {
                            // En modo eliminación, resaltar nave completa
                            resaltarNaveParaEliminar(filaFinal, colFinal);
                        } else if (naveSeleccionada != null) {
                            resaltarPosicion(filaFinal, colFinal);
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (modoEliminacion) {
                            limpiarResaltadoEliminacion();
                        } else if (naveSeleccionada != null) {
                            limpiarResaltado();
                        }
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (modoEliminacion) {
                            // En modo eliminación, seleccionar nave para eliminar
                            seleccionarNaveParaEliminar(filaFinal, colFinal);
                        } else if (naveSeleccionada != null) {
                            colocarNave(filaFinal, colFinal);
                        }
                    }
                };

                btn.addMouseListener(mouseListener);
                listenersMouse.put(coordKey, mouseListener);

                panelTablero.add(btn);
                botonesTablero[fila][col] = btn;
            }
        }

        panelTablero.setPreferredSize(new Dimension(tamañoTablero * buttonSize, tamañoTablero * buttonSize));
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    /**
     * Cargar icono desde recursos
     */
    private ImageIcon cargarIcono(String ruta) {
        try {
            return new ImageIcon(getClass().getResource(ruta));
        } catch (Exception e) {
            System.err.println("Error cargando icono: " + ruta);
            return null;
        }
    }

    /**
     * Crear icono de color sólido
     */
    private ImageIcon crearIconoColor(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, width - 1, height - 1);
        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * Rotar orientación
     */
    private void rotarOrientacion() {
        orientacionHorizontal = !orientacionHorizontal;
        String orientacion = orientacionHorizontal ? "Horizontal" : "Vertical";
        System.out.println("Orientación cambiada a: " + orientacion);

        // Mostrar mensaje temporal
        JOptionPane.showMessageDialog(this,
                "Orientación: " + orientacion,
                "Orientación cambiada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Resaltar posición donde se colocaría la nave
     */
    private void resaltarPosicion(int fila, int columna) {
        if (naveSeleccionada == null) {
            return;
        }

        int tamaño = obtenerTamañoNave(naveSeleccionada);

        // Limpiar resaltado anterior
        limpiarResaltado();

        // Crear iconos para resaltado
        ImageIcon iconResaltado = crearIconoColor(Color.YELLOW, 60, 60);
        ImageIcon iconInvalido = crearIconoColor(Color.RED, 60, 60);

        if (orientacionHorizontal) {
            // Verificar si cabe horizontalmente
            if (columna + tamaño > tamañoTablero) {
                // Mostrar posición inválida
                for (int i = 0; i < Math.min(tamaño, tamañoTablero - columna); i++) {
                    botonesTablero[fila][columna + i].setIcon(iconInvalido);
                }
                return;
            }

            // Verificar si es posición válida (sin colisiones y con espacio alrededor)
            boolean valida = esPosicionValida(fila, columna, tamaño);

            // Resaltar posición
            for (int i = 0; i < tamaño; i++) {
                botonesTablero[fila][columna + i].setIcon(valida ? iconResaltado : iconInvalido);
            }
        } else {
            // Verificar si cabe verticalmente
            if (fila + tamaño > tamañoTablero) {
                // Mostrar posición inválida
                for (int i = 0; i < Math.min(tamaño, tamañoTablero - fila); i++) {
                    botonesTablero[fila + i][columna].setIcon(iconInvalido);
                }
                return;
            }

            // Verificar si es posición válida (sin colisiones y con espacio alrededor)
            boolean valida = esPosicionValida(fila, columna, tamaño);

            // Resaltar posición
            for (int i = 0; i < tamaño; i++) {
                botonesTablero[fila + i][columna].setIcon(valida ? iconResaltado : iconInvalido);
            }
        }
    }

    /**
     * Activar modo eliminación
     */
    private void activarModoEliminacion() {
        modoEliminacion = true;
        naveAEliminar = null;
        btnEliminar.setEnabled(false);
        btnEliminar.setBackground(Color.GRAY);
        btnCancelarEliminacion.setVisible(true);

        // Cambiar instrucciones
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JLabel && comp.getBounds().y == 50) {
                ((JLabel) comp).setText("Haz clic en una nave para eliminarla");
                ((JLabel) comp).setForeground(Color.ORANGE);
                break;
            }
        }

        // Resaltar naves colocadas
        for (int fila = 0; fila < tamañoTablero; fila++) {
            for (int col = 0; col < tamañoTablero; col++) {
                JButton btn = botonesTablero[fila][col];
                if (btn.getClientProperty("nave") != null) {
                    btn.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
                }
            }
        }

        JOptionPane.showMessageDialog(this,
                "Modo eliminación activado.\n"
                + "Haz clic en una nave para eliminarla.\n"
                + "Presiona 'Cancelar Eliminación' para salir de este modo.",
                "Modo Eliminación",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Desactivar modo eliminación
     */
    private void desactivarModoEliminacion() {
        modoEliminacion = false;
        naveAEliminar = null;
        btnEliminar.setEnabled(true);
        btnEliminar.setBackground(Color.ORANGE);
        btnCancelarEliminacion.setVisible(false);

        // Restaurar instrucciones
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JLabel && comp.getBounds().y == 50) {
                ((JLabel) comp).setText("Selecciona una nave y haz clic en el tablero");
                ((JLabel) comp).setForeground(Color.WHITE);
                break;
            }
        }

        // Restaurar bordes normales
        for (int fila = 0; fila < tamañoTablero; fila++) {
            for (int col = 0; col < tamañoTablero; col++) {
                JButton btn = botonesTablero[fila][col];
                btn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
            }
        }

        limpiarResaltado();
    }

    /**
     * Resaltar nave para eliminación
     */
    private void resaltarNaveParaEliminar(int fila, int columna) {
        Nave nave = obtenerNaveEnPosicion(fila, columna);
        if (nave != null) {
            // Resaltar todas las celdas de esta nave
            for (String coordenada : nave.getCoordenadas()) {
                String[] parts = coordenada.split(",");
                int f = Integer.parseInt(parts[0]) - 1;
                int c = Integer.parseInt(parts[1]) - 1;

                if (f >= 0 && f < tamañoTablero && c >= 0 && c < tamañoTablero) {
                    botonesTablero[f][c].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                }
            }
        }
    }

    /**
     * Limpiar resaltado de eliminación
     */
    private void limpiarResaltadoEliminacion() {
        for (int fila = 0; fila < tamañoTablero; fila++) {
            for (int col = 0; col < tamañoTablero; col++) {
                JButton btn = botonesTablero[fila][col];
                btn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
            }
        }
    }

    /**
     * Seleccionar nave para eliminación
     */
    private void seleccionarNaveParaEliminar(int fila, int columna) {
        Nave nave = obtenerNaveEnPosicion(fila, columna);
        if (nave != null) {
            eliminarNave(nave);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No hay una nave en esta posición.",
                    "Sin nave",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Eliminar nave seleccionada
     */
    private void eliminarNave(Nave nave) {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres eliminar esta nave?\n"
                + "Tipo: " + nave.getTipoNave() + "\n"
                + "Posición: " + nave.getCoordenadas().toString(),
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            // Remover nave de la lista
            navesColocadas.remove(nave);

            // Actualizar contador
            contadorNaves.put(nave.getTipoNave(), contadorNaves.get(nave.getTipoNave()) - 1);

            // Limpiar casillas del tablero
            ImageIcon iconAgua = cargarIcono("/imagenes/CasillaAgua.png");
            for (String coordenada : nave.getCoordenadas()) {
                String[] parts = coordenada.split(",");
                int fila = Integer.parseInt(parts[0]) - 1;
                int col = Integer.parseInt(parts[1]) - 1;

                if (fila >= 0 && fila < tamañoTablero && col >= 0 && col < tamañoTablero) {
                    botonesTablero[fila][col].setIcon(iconAgua != null ? iconAgua : crearIconoColor(new Color(173, 216, 230), 60, 60));
                    botonesTablero[fila][col].setBackground(new Color(173, 216, 230));
                    botonesTablero[fila][col].putClientProperty("nave", null);
                }
            }

            // Actualizar interfaz
            actualizarBotonesNaves();
            actualizarEstadoColocacion();
            desactivarModoEliminacion();

            JOptionPane.showMessageDialog(this,
                    "Nave eliminada correctamente.\n"
                    + "Puedes volver a colocarla.",
                    "Nave eliminada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Obtener nave en posición específica
     */
    private Nave obtenerNaveEnPosicion(int fila, int columna) {
        for (Nave nave : navesColocadas) {
            for (String coordenada : nave.getCoordenadas()) {
                String[] parts = coordenada.split(",");
                int f = Integer.parseInt(parts[0]) - 1;
                int c = Integer.parseInt(parts[1]) - 1;
                if (f == fila && c == columna) {
                    return nave;
                }
            }
        }
        return null;
    }

    /**
     * Verificar si la celda es parte de una nave ya colocada
     */
    private boolean esParteDeNave(int fila, int col) {
        return obtenerNaveEnPosicion(fila, col) != null;
    }

    /**
     * Verificar si hay una nave adyacente (incluyendo diagonales)
     */
    private boolean hayNaveAdyacente(int fila, int col) {
        // Verificar las 8 direcciones alrededor de la celda
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nuevaFila = fila + i;
                int nuevaCol = col + j;

                // Verificar límites del tablero
                if (nuevaFila >= 0 && nuevaFila < tamañoTablero
                        && nuevaCol >= 0 && nuevaCol < tamañoTablero) {
                    // No verificar la celda actual
                    if (i != 0 || j != 0) {
                        if (esParteDeNave(nuevaFila, nuevaCol)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Limpiar resaltado del tablero
     */
    private void limpiarResaltado() {
        ImageIcon iconAgua = cargarIcono("/imagenes/CasillaAgua.png");
        ImageIcon iconAguaFinal = iconAgua != null ? iconAgua : crearIconoColor(new Color(173, 216, 230), 60, 60);

        for (int fila = 0; fila < tamañoTablero; fila++) {
            for (int col = 0; col < tamañoTablero; col++) {
                JButton btn = botonesTablero[fila][col];

                // Si es parte de una nave colocada, mantener su color
                if (!esParteDeNave(fila, col)) {
                    btn.setIcon(iconAguaFinal);
                    btn.setBackground(new Color(173, 216, 230));
                }
            }
        }
    }

    /**
     * Colocar nave en el tablero
     */
    private void colocarNave(int fila, int columna) {
        if (naveSeleccionada == null) {
            return;
        }

        int tamaño = obtenerTamañoNave(naveSeleccionada);

        if (!esPosicionValida(fila, columna, tamaño)) {
            JOptionPane.showMessageDialog(this,
                    "Posición inválida!\n"
                    + "Razones posibles:\n"
                    + "1. La nave se sale del tablero\n"
                    + "2. Se superpone con otra nave\n"
                    + "3. Está demasiado cerca de otra nave\n"
                    + "Intenta en otra posición.",
                    "Error de colocación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> coordenadas = new ArrayList<>();
        Color colorNave = obtenerColorNave(naveSeleccionada);
        ImageIcon iconNave = crearIconoColor(colorNave, 60, 60);

        if (orientacionHorizontal) {
            for (int i = 0; i < tamaño; i++) {
                coordenadas.add((fila + 1) + "," + (columna + i + 1));
                botonesTablero[fila][columna + i].setIcon(iconNave);
                botonesTablero[fila][columna + i].setBackground(colorNave);
                botonesTablero[fila][columna + i].putClientProperty("nave", naveSeleccionada);
            }
        } else {
            for (int i = 0; i < tamaño; i++) {
                coordenadas.add((fila + i + 1) + "," + (columna + 1));
                botonesTablero[fila + i][columna].setIcon(iconNave);
                botonesTablero[fila + i][columna].setBackground(colorNave);
                botonesTablero[fila + i][columna].putClientProperty("nave", naveSeleccionada);
            }
        }

        Nave nave = new Nave(naveSeleccionada, coordenadas, 0, EstadoNave.ACTIVA);
        navesColocadas.add(nave);

        // Actualizar contador
        contadorNaves.put(naveSeleccionada, contadorNaves.get(naveSeleccionada) + 1);

        System.out.println("Nave colocada: " + naveSeleccionada
                + " en posición: " + coordenadas.toString());

        // Reiniciar selección
        naveSeleccionada = null;
        limpiarResaltado();

        // Actualizar interfaz
        actualizarBotonesNaves();
        actualizarEstadoColocacion();
    }

    /**
     * Verificar si la posición es válida para colocar la nave
     */
    private boolean esPosicionValida(int fila, int columna, int tamaño) {
        // Verificar límites del tablero
        if (orientacionHorizontal) {
            if (columna + tamaño > tamañoTablero) {
                return false;
            }

            // Verificar cada celda que ocuparía la nave
            for (int i = 0; i < tamaño; i++) {
                // Verificar si hay nave en esta celda
                if (esParteDeNave(fila, columna + i)) {
                    return false;
                }

                // Verificar si hay nave adyacente (incluyendo diagonales)
                if (hayNaveAdyacente(fila, columna + i)) {
                    return false;
                }
            }
        } else {
            if (fila + tamaño > tamañoTablero) {
                return false;
            }

            // Verificar cada celda que ocuparía la nave
            for (int i = 0; i < tamaño; i++) {
                // Verificar si hay nave en esta celda
                if (esParteDeNave(fila + i, columna)) {
                    return false;
                }

                // Verificar si hay nave adyacente (incluyendo diagonales)
                if (hayNaveAdyacente(fila + i, columna)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Actualizar el estado de colocación - ACTUALIZADO para 11 naves
     */
    private void actualizarEstadoColocacion() {
        int navesColocadasCount = navesColocadas.size();
        lblEstado.setText("Naves colocadas: " + navesColocadasCount + "/" + totalNavesRequeridas);

        // Habilitar botón LISTO si todas las naves están colocadas
        if (navesColocadasCount == totalNavesRequeridas) {
            todasNavesColocadas = true;
            btnListo.setEnabled(true);
            btnListo.setBackground(new Color(0, 200, 0));

            System.out.println("¡Todas las naves colocadas! Botón LISTO habilitado.");
        } else {
            btnListo.setEnabled(false);
            btnListo.setBackground(new Color(0, 180, 0));
        }
    }

    /**
     * Finalizar colocación y pasar al siguiente estado - ACTUALIZADO para nueva
     * distribución
     */
    private void finalizarColocacion() {
        // Validar que se colocaron todas las naves requeridas
        if (navesColocadas.size() < totalNavesRequeridas) {
            JOptionPane.showMessageDialog(this,
                    "Debes colocar todas las naves primero (" + navesColocadas.size() + "/" + totalNavesRequeridas + ")\n"
                    + "Faltan " + (totalNavesRequeridas - navesColocadas.size()) + " naves por colocar",
                    "Naves pendientes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar que se colocó la cantidad correcta de cada tipo
        StringBuilder resumen = new StringBuilder("Resumen de naves colocadas:\n");
        boolean todasCorrectas = true;

        for (TipoNave tipo : TipoNave.values()) {
            int colocadas = contadorNaves.getOrDefault(tipo, 0);
            int requeridas = obtenerMaximoPorTipo(tipo);

            resumen.append("- ").append(obtenerNombreTipo(tipo))
                    .append(": ").append(colocadas).append("/").append(requeridas).append("\n");

            if (colocadas != requeridas) {
                todasCorrectas = false;
            }
        }

        if (!todasCorrectas) {
            JOptionPane.showMessageDialog(this,
                    "No has colocado la cantidad correcta de naves:\n\n"
                    + resumen.toString()
                    + "\nPor favor, coloca todas las naves requeridas.",
                    "Naves incompletas",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmar con el usuario
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres finalizar la colocación?\n"
                + "No podrás cambiar las posiciones después.\n\n"
                + resumen.toString(),
                "Confirmar finalización",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            System.out.println("✅ Colocación finalizada - Jugador: "
                    + (controlJuego != null ? controlJuego.getMiNombre() : "Desconocido"));

            // Guardar naves en el tablero local
            if (tablero != null) {
                tablero.setNaves(navesColocadas);
                System.out.println("📊 Naves guardadas en tablero: " + navesColocadas.size());

                // Colocar naves en casillas del tablero
                try {
                    tablero.colocarNavesEnCasillas();
                    System.out.println("🎯 Naves colocadas en casillas del tablero");
                } catch (Exception e) {
                    System.err.println("⚠️ Error al colocar naves en casillas: " + e.getMessage());
                }
            }

            // Si hay controlJuego, también guardar en el jugador local
            if (controlJuego != null && controlJuego.getJugadorLocal() != null) {
                Jugador jugador = controlJuego.getJugadorLocal();
                if (jugador.getTableros() != null && !jugador.getTableros().isEmpty()) {
                    Tablero tableroJugador = jugador.getTableros().get(0);
                    tableroJugador.setNaves(navesColocadas);
                    try {
                        tableroJugador.colocarNavesEnCasillas();
                        System.out.println("👤 Naves guardadas en jugador local: " + jugador.getNombre());
                    } catch (Exception e) {
                        System.err.println("⚠️ Error al colocar naves en jugador: " + e.getMessage());
                    }
                }
            }

            // NOTIFICAR QUE LA COLOCACIÓN TERMINÓ - MODIFICADO para pasar las naves
            if (controlJuego != null) {
                try {
                    // Pasar las naves colocadas a ControlJuego
                    controlJuego.finalizarColocacionJugador(navesColocadas); // ← ¡AQUÍ EL CAMBIO!
                    System.out.println("📡 Notificación de colocación enviada al oponente");
                } catch (Exception e) {
                    System.err.println("❌ Error al notificar colocación: " + e.getMessage());

                    // Mostrar error específico
                    String mensajeError;
                    if (e.getMessage().contains("finalizarColocacionJugador")) {
                        mensajeError = "Error: El método finalizarColocacionJugador no acepta parámetros.\n"
                                + "Necesitas modificar ControlJuego para aceptar la lista de naves.";
                    } else {
                        mensajeError = e.getMessage();
                    }

                    JOptionPane.showMessageDialog(this,
                            "Error al notificar al oponente:\n" + mensajeError + "\n\n"
                            + "La colocación se guardó localmente, pero la partida no comenzará\n"
                            + "hasta que el oponente se entere manualmente.",
                            "Error de comunicación",
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                System.err.println("⚠️ controlJuego es null - No se puede notificar al oponente");

                // Si no hay controlJuego (modo local), mostrar opción para continuar
                if (controlJuego == null) {
                    int continuar = JOptionPane.showConfirmDialog(this,
                            "Modo local detectado.\n\n"
                            + "¿Quieres continuar con la partida en modo local?\n"
                            + "(Necesitarás abrir manualmente el PanelJuego)",
                            "Modo Local",
                            JOptionPane.YES_NO_OPTION);

                    if (continuar == JOptionPane.YES_OPTION) {
                        System.out.println("🎮 Continuando en modo local...");
                        // Aquí podrías llamar a un método para abrir PanelJuego localmente
                        abrirPantallaDeJuegoLocal();
                    }
                }
            }

            // Mostrar mensaje de espera
            String mensajeEspera;
            if (controlJuego != null && controlJuego.getServerId() != null) {
                mensajeEspera = "✅ ¡Colocación completada exitosamente!\n\n"
                        + "Resumen:\n" + resumen.toString() + "\n"
                        + "🕐 Esperando que el oponente termine su colocación...\n"
                        + "📡 Estado: " + (controlJuego.isSoyServidor() ? "Servidor" : "Cliente") + "\n"
                        + "Oponente: " + controlJuego.getNombreOponente() + "\n\n"
                        + "Cuando ambos jugadores estén listos,\n"
                        + "la partida comenzará automáticamente.";
            } else {
                mensajeEspera = "✅ ¡Colocación completada exitosamente!\n\n"
                        + "Resumen:\n" + resumen.toString() + "\n"
                        + "🎮 Modo local - Listo para jugar\n\n"
                        + "La partida comenzará cuando ambos jugadores\n"
                        + "completen su colocación manualmente.";
            }

            JOptionPane.showMessageDialog(this,
                    mensajeEspera,
                    "Colocación Finalizada",
                    JOptionPane.INFORMATION_MESSAGE);

            System.out.println("🚪 Cerrando ventana de colocación...");
            this.dispose();

            // IMPORTANTE: NO abrir PanelJuego aquí directamente
            // Se abrirá automáticamente cuando ControlVista reciba la notificación
            // a través del listener en ControlJuego
            System.out.println("⏳ Esperando sincronización con oponente...");

            // Si estamos en modo local (sin conexión P2P), podemos abrir PanelJuego directamente
            // Pero esto solo pasa en pruebas locales
            if (controlJuego == null || controlJuego.getServerId() == null) {
                System.out.println("🎮 Modo local - La partida comenzará cuando ambos jugadores terminen");
                // En modo local, cada jugador debe terminar su colocación manualmente
                // y alguien tendría que abrir PanelJuego manualmente
            }
        } else {
            System.out.println("❌ Finalización cancelada por el usuario");
        }
    }

// Método auxiliar para abrir PanelJuego en modo local
    private void abrirPantallaDeJuegoLocal() {
        try {
            System.out.println("🚀 Abriendo PanelJuego en modo local...");

            // Aquí necesitas crear la lógica para abrir PanelJuego en modo local
            // Esto depende de cómo tengas configurado tu proyecto
            // Ejemplo básico:
            /*
        if (controlJuego != null) {
            // Crear PartidaController local
            PartidaController partidaController = new PartidaController(controlJuego, controlJuego.getJugadorLocal());
            
            // Crear PanelJuego
            PanelJuego panelJuego = new PanelJuego(partidaController);
            panelJuego.setTitle("🚢 BATTLESHIP - Modo Local");
            panelJuego.setSize(1400, 800);
            panelJuego.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            panelJuego.setLocationRelativeTo(null);
            panelJuego.setVisible(true);
            
            System.out.println("✅ PanelJuego abierto en modo local");
        } else {
            System.err.println("❌ No se puede abrir PanelJuego: controlJuego es null");
        }
             */
        } catch (Exception e) {
            System.err.println("❌ Error al abrir PanelJuego local: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al abrir la pantalla de juego:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

// Método auxiliar para abrir PanelJuego (si es necesario para modo local)
    private void abrirPantallaDeJuego() {
        try {
            System.out.println("🚀 Redirigiendo a pantalla de juego...");

            // Código para abrir PanelJuego - depende de tu implementación
            // Por ejemplo:
            // PanelJuego panelJuego = new PanelJuego(partidaController);
            // panelJuego.setVisible(true);
        } catch (Exception e) {
            System.err.println("❌ Error al abrir pantalla de juego: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al abrir la pantalla de juego:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método para obtener las naves colocadas
     */
    public List<Nave> getNavesColocadas() {
        return new ArrayList<>(navesColocadas);
    }

    /**
     * Método para obtener el tablero con las naves colocadas
     */
    public Tablero getTableroConNaves() {
        if (tablero != null) {
            tablero.setNaves(navesColocadas);
        }
        return tablero;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelFondo = new javax.swing.JLabel();
        panelTablero = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelFondo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/PantallaColocacion.png"))); // NOI18N
        getContentPane().add(jLabelFondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        getContentPane().add(panelTablero, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 100, 600, 600));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(PanelColocacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PanelColocacion().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelFondo;
    private javax.swing.JPanel panelTablero;
    // End of variables declaration//GEN-END:variables
}
