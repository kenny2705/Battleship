package controllers;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import models.Casilla;
import models.Tablero;
import models.enums.ResultadoDisparo;

/**
 *
 * @author Usuario
 */
public class ControlVista {
    
    private ControlJuego controlJuego;

    public ControlVista(ControlJuego controlJuego) {
        this.controlJuego = controlJuego;
    }
    
    public void generarTablero(Tablero tablero, JPanel panelTablero, ControlJuego controlJuego) {
        int n = tablero.getMedidas();
        int buttonSize = 60;
        panelTablero.removeAll();

        // --- INICIO DE CÓDIGO DE DIAGNÓSTICO ---
        try {
            String rutaImagen = "/imagenes/CasillaAgua.png";
            java.net.URL urlTest = getClass().getResource(rutaImagen);

            if (urlTest == null) {
                System.out.println("---------------------------------------------------------");
                System.out.println("¡ERROR DE DIAGNÓSTICO! No se encontró el recurso: " + rutaImagen);
                System.out.println("Asegúrate de que la carpeta 'imagenes' esté en la raíz del 'Source Path' (usualmente la carpeta 'src').");
                System.out.println("---------------------------------------------------------");
            } else {
                System.out.println("Recurso encontrado exitosamente en: " + urlTest.toExternalForm());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
// --- FIN DE CÓDIGO DE DIAGNÓSTICO ---

// Tu línea original que da el error
       // ImageIcon iconAgua = new ImageIcon(getClass().getResource("/imagenes/CasillaAgua.png"));
// ... resto de tu código ...

        ImageIcon iconAgua = new ImageIcon(getClass().getResource("/imagenes/CasillaAgua.png"));
        ImageIcon iconAcierto = new ImageIcon(getClass().getResource("/imagenes/DisparoAcertado.png"));
        //ImageIcon iconHundido = new ImageIcon(getClass().getResource("/imagenes/NaveHundida.png"));
        ImageIcon iconFallo = new ImageIcon(getClass().getResource("/imagenes/DisparoFallido.png"));

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                String coordenada = convertirCoordenada(fila, col);
                Casilla casilla = new Casilla(coordenada, false, false);
                tablero.getMatrizDeCasillas().add(casilla);

                JButton btn = new JButton();
                btn.setBounds(col * buttonSize, fila * buttonSize, buttonSize, buttonSize);
                btn.setActionCommand(coordenada);
                btn.setIcon(iconAgua);

                btn.addActionListener(e -> {
                    ResultadoDisparo resultado = controlJuego.realizarDisparo(tablero, coordenada);

                    switch (resultado) {
                        case AGUA ->
                            btn.setIcon(iconFallo);
                        case IMPACTO ->
                            btn.setIcon(iconAcierto);
                        //case HUNDIDO ->
                           // btn.setIcon(iconHundido);
                    }

                    // Evitar múltiples disparos en el mismo botón
                    for (var al : btn.getActionListeners()) {
                        btn.removeActionListener(al);
                    }

                    System.out.println("Disparo en " + coordenada + "--"  + resultado);
                });

                panelTablero.add(btn);
            }
        }
        tablero.colocarNavesEnCasillas();

        panelTablero.setPreferredSize(new java.awt.Dimension(n * buttonSize, n * buttonSize));
        panelTablero.revalidate();
        panelTablero.repaint();
    }
    //Pasa las coordenadas de numeros a letra y numero
    private String convertirCoordenada(int fila, int col) {
        char letra = (char) ('A' + fila); 
        return letra + String.valueOf(col + 1);
    }
}
