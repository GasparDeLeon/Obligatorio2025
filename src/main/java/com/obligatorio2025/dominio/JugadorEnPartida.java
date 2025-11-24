package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoJugador;
import java.util.ArrayList;
import java.util.List;

public class JugadorEnPartida {

    private int usuarioId;
    private List<Respuesta> respuestas;
    private EstadoJugador estado;
    private boolean listo;

    public JugadorEnPartida(int usuarioId) {
        this.usuarioId = usuarioId;
        this.respuestas = new ArrayList<>();
        this.estado = EstadoJugador.INACTIVO;
        this.listo = false;
    }

    // alias compatible con Sala
    public int getJugadorId() {
        return usuarioId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public List<Respuesta> getRespuestas() {
        return respuestas;
    }

    public EstadoJugador getEstado() {
        return estado;
    }

    public void agregarRespuesta(Respuesta respuesta) {
        this.respuestas.add(respuesta);
    }

    public void marcarListo() {
        this.estado = EstadoJugador.LISTO;
        this.listo = true;
    }

    public void rendirse() {
        this.estado = EstadoJugador.RENDIDO;
    }

    public void desconectar() {
        this.estado = EstadoJugador.DESCONECTADO;
    }

    public boolean isListo() {
        return listo;
    }

    public void setListo(boolean listo) {
        this.listo = listo;
    }

    public String getNombreVisible() {
        return "Jugador " + getJugadorId();
    }
}
