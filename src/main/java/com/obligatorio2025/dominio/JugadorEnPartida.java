package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoJugador;

import java.util.ArrayList;
import java.util.List;

public class JugadorEnPartida {

    private String usuarioId;
    private List<Respuesta> respuestas;
    private EstadoJugador estado;

    public JugadorEnPartida(String usuarioId) {
        this.usuarioId = usuarioId;
        this.respuestas = new ArrayList<>();
        this.estado = EstadoJugador.INACTIVO;
    }

    public String getUsuarioId() {
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
    }

    public void rendirse() {
        this.estado = EstadoJugador.RENDIDO;
    }

    public void desconectar() {
        this.estado = EstadoJugador.DESCONECTADO;
    }
}
