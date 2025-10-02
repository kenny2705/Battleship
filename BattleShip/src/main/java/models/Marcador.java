/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author Usuario
 */
public class Marcador {
    private int navesIntactas;
    private int navesAveriadas;
    private int navesHundidas;
    private int totalDisparos;
    private int aciertos;

    public Marcador() {
    }

    public Marcador(int navesIntactas, int navesAveriadas, int navesHundidas, int totalDisparos, int aciertos) {
        this.navesIntactas = navesIntactas;
        this.navesAveriadas = navesAveriadas;
        this.navesHundidas = navesHundidas;
        this.totalDisparos = totalDisparos;
        this.aciertos = aciertos;
    }

    public int getNavesIntactas() {
        return navesIntactas;
    }

    public void setNavesIntactas(int navesIntactas) {
        this.navesIntactas = navesIntactas;
    }

    public int getNavesAveriadas() {
        return navesAveriadas;
    }

    public void setNavesAveriadas(int navesAveriadas) {
        this.navesAveriadas = navesAveriadas;
    }

    public int getNavesHundidas() {
        return navesHundidas;
    }

    public void setNavesHundidas(int navesHundidas) {
        this.navesHundidas = navesHundidas;
    }

    public int getTotalDisparos() {
        return totalDisparos;
    }

    public void setTotalDisparos(int totalDisparos) {
        this.totalDisparos = totalDisparos;
    }

    public int getAciertos() {
        return aciertos;
    }

    public void setAciertos(int aciertos) {
        this.aciertos = aciertos;
    }
    
    public int calculaPuntos(int navesAveriadas,int navesHundidas ){
        int puntos =0;
        return puntos;
    }
    
}
