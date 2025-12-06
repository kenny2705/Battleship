package battleship.infrastructure.ui;

import battleship.domain.model.Tablero;
import battleship.domain.model.TableroObservador;

public class TableroObservadorUI implements TableroObservador {
    private final ActualizadorUI actualizadorUI;

    public TableroObservadorUI(ActualizadorUI actualizadorUI) {
        this.actualizadorUI = actualizadorUI;
    }

    @Override
    public void actualizarTablero(Tablero tablero, String mensaje) {
        actualizadorUI.actualizarVista(tablero, mensaje);
    }

    public interface ActualizadorUI {
        void actualizarVista(Tablero tablero, String mensaje);
    }
}