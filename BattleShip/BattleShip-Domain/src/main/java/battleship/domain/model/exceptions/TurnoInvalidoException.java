/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package battleship.domain.model.exceptions;

public class TurnoInvalidoException extends RuntimeException {
    public TurnoInvalidoException(String mensaje) {
        super(mensaje);
    }
}

