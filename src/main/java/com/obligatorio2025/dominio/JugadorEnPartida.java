package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoJugador;

public class JugadorEnPartida {

    private String idUsuario;      // o CI, o lo que uses
    private String nombre;
    private EstadoJugador estado;
    private int puntaje;

    public JugadorEnPartida(String idUsuario, String nombre) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.estado = EstadoJugador.INVITADO;
        this.puntaje = 0;
    }

    public void marcarListo() {
        this.estado = EstadoJugador.LISTO;
    }

    public void sumarPuntos(int puntos) {
        this.puntaje += puntos;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoJugador getEstado() {
        return estado;
    }

    public int getPuntaje() {
        return puntaje;
    }
}
