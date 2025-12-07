/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package controllers;

/**
 *
 * @author Acer
 */
public interface IControlVista {

    /**
     * Envía un mensaje a través de la red P2P.
     *
     * @param msg El mensaje a enviar.
     * @throws Exception Si ocurre un error de envío.
     */
    void enviarMensaje(String msg) throws Exception;

    /**
     * Notifica a la vista que el rival ha terminado el acomodo.
     */
    void notificarRivalListo();

    /**
     * Muestra un mensaje de alerta al usuario.
     *
     * @param mensaje El texto del mensaje.
     */
    void mostrarMensaje(String mensaje);

    /**
     * Fuerza la actualización visual de los tableros en la vista.
     */
    void actualizarTableros();
}
