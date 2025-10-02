package controllers;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import models.Casilla;
import models.Tablero;
import models.enums.ResultadoDisparo;
import static models.enums.ResultadoDisparo.AGUA;
import static models.enums.ResultadoDisparo.IMPACTO;

/**
 *
 * @author Usuario
 */
public class ControlVista {

    private ControlJuego controlJuego;
    private Map<String, JButton> botones;

    public ControlVista(ControlJuego controlJuego) {
        this.controlJuego = controlJuego;
        this.botones = new HashMap<>();
    }

    public void generarTablero(Tablero tablero, JPanel panelTablero) {
        int n = tablero.getMedidas();
        int buttonSize = 60;
        panelTablero.removeAll();
        botones.clear();

        ImageIcon iconAgua = new ImageIcon(getClass().getResource("/imagenes/CasillaAgua.png"));
        ImageIcon iconAcierto = new ImageIcon(getClass().getResource("/imagenes/DisparoAcertado.png"));
        //ImageIcon iconHundido = new ImageIcon(getClass().getResource("/imagenes/NaveHundida.png"));
        ImageIcon iconFallo = new ImageIcon(getClass().getResource("/imagenes/DisparoFallido.png"));

        tablero.colocarNavesEnCasillas();

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coordenada = convertirCoordenada(fila, col);
                Casilla casilla = tablero.getMatrizDeCasillas().get(fila * n + col);

                JButton btn = new JButton();
                btn.setBounds(col * buttonSize, fila * buttonSize, buttonSize, buttonSize);
                btn.setActionCommand(coordenada);

                btn.setIcon(casilla.isDañada() ? (casilla.isOcupada() ? iconAcierto : iconFallo) : iconAgua);
                btn.setEnabled(!casilla.isDañada());
                btn.addActionListener(e -> {
                    if (!casilla.isDañada()) {
                        // Actualiza el modelo y obtiene resultado
                        controlJuego.realizarDisparo(tablero, coordenada);
                    }
                });

                panelTablero.add(btn);
                botones.put(coordenada, btn);
            }
        }
        panelTablero.setPreferredSize(new Dimension(n * buttonSize, n * buttonSize));
        panelTablero.revalidate();
        panelTablero.repaint();

//        panelTablero.setPreferredSize(new java.awt.Dimension(n * buttonSize, n * buttonSize));
//        panelTablero.revalidate();
//        panelTablero.repaint();
    }

    public void actualizarBotones(Tablero tablero) {
        ImageIcon iconAgua = new ImageIcon(getClass().getResource("/imagenes/CasillaAgua.png"));
        ImageIcon iconAcierto = new ImageIcon(getClass().getResource("/imagenes/DisparoAcertado.png"));
        ImageIcon iconFallo = new ImageIcon(getClass().getResource("/imagenes/DisparoFallido.png"));

        for (Casilla casilla : tablero.getMatrizDeCasillas()) {
            JButton btn = botones.get(casilla.getCoordenada());
            if (btn != null) {
                btn.setIcon(casilla.isDañada() ? (casilla.isOcupada() ? iconAcierto : iconFallo) : iconAgua);
                btn.setEnabled(!casilla.isDañada());
                
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setOpaque(false);
            }
        }
    }

    public Map<String, JButton> getBotones() {
        return botones;
    }

    //Pasa las coordenadas de numeros a letra y numero
    private String convertirCoordenada(int fila, int col) {
        char letra = (char) ('A' + fila);
        return letra + String.valueOf(col + 1);
    }

}
