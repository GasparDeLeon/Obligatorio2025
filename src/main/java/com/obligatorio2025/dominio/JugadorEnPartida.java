package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoJugador;
import java.util.ArrayList;
import java.util.List;

public class JugadorEnPartida {

    private int usuarioId;
    private String nombreUsuario;
    private List<Respuesta> respuestas;
    private EstadoJugador estado;
    private boolean listo;

    public JugadorEnPartida(int usuarioId) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = null;
        this.respuestas = new ArrayList<>();
        this.estado = EstadoJugador.INACTIVO;
        this.listo = false;
    }

    public JugadorEnPartida(int usuarioId, String nombreUsuario) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
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

    /**
     * Returns the visible name for this player.
     * Prioritizes nombreUsuario if available, otherwise falls back to "Jugador X".
     */
    public String getNombreVisible() {
        if (nombreUsuario != null && !nombreUsuario.isBlank()) {
            return nombreUsuario;
        }
        return "Jugador " + getJugadorId();
    }
}
