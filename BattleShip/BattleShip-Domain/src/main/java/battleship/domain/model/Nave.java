package battleship.domain.model;

import battleship.domain.enums.EstadoNave;
import battleship.domain.enums.ResultadoDisparo;
import battleship.domain.enums.TipoNave;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nave {

    private TipoNave tipoNave;
    private List<String> coordenadas;
    private int impactos;
    private EstadoNave estadoNave;
    private final Set<String> coordenadasImpactadas = new HashSet<>();

    public Nave() {
    }

    public Nave(TipoNave tipoNave, List<String> coordenadas, int impactos, EstadoNave estadoNave) {
        this.tipoNave = tipoNave;
        this.coordenadas = coordenadas;
        this.impactos = impactos;
        this.estadoNave = estadoNave;
    }

    public boolean naveHundida() {
        return estadoNave == EstadoNave.HUNDIDA;
    }

    public void actualizaEstado() {
        if (impactos >= getTamano()) {
            estadoNave = EstadoNave.HUNDIDA;
        } else if (impactos > 0) {
            estadoNave = EstadoNave.DAÑADA;
        } else {
            estadoNave = EstadoNave.ACTIVA;
        }
    }

    public ResultadoDisparo recibirImpacto(String coordenada) {
        if (!ocupa(coordenada)) {
            return ResultadoDisparo.AGUA;
        }

        if (coordenadasImpactadas.contains(coordenada)) {
            return ResultadoDisparo.REPETIDO;
        }

        coordenadasImpactadas.add(coordenada);
        impactos = coordenadasImpactadas.size();

        actualizaEstado();

        return naveHundida() ? ResultadoDisparo.HUNDIDO : ResultadoDisparo.IMPACTO;
    }

    public boolean ocupa(String coordenada) {
        return coordenadas != null && coordenadas.contains(coordenada);
    }

    public boolean estaHundida() {
        return naveHundida();
    }

    public int getTamano() {
        return switch (tipoNave) {
            case BARCO ->
                1;
            case SUBMARINO ->
                2;
            case CRUCERO ->
                3;
            case PORTA_AVIONES ->
                4;
        };
    }

    public TipoNave getTipoNave() {
        return tipoNave;
    }

    public void setTipoNave(TipoNave tipoNave) {
        this.tipoNave = tipoNave;
    }

    public List<String> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<String> coordenadas) {
        this.coordenadas = coordenadas;
    }

    public int getImpactos() {
        return impactos;
    }

    public void setImpactos(int impactos) {
        this.impactos = impactos;
    }

    public EstadoNave getEstadoNave() {
        return estadoNave;
    }

    public void setEstadoNave(EstadoNave estadoNave) {
        this.estadoNave = estadoNave;
    }
}
