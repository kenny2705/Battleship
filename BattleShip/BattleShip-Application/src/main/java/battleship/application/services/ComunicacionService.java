/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package battleship.application.services;

public interface ComunicacionService {
    // Modo servidor
    void startAsServer();
    String getServerId();
    
    // Modo cliente  
    void connectToServer(String serverId) throws Exception;
    
    // Envío de mensajes
    void sendMessage(String message) throws Exception;
    
    // Configuración listeners
    void setMessageListener(MessageListener listener);
    void setConnectionListener(ConnectionListener listener);
    
    // Detener conexión
    void stop();
}
