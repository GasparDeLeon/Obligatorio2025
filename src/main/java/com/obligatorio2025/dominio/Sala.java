package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoSala;

import java.util.ArrayList;
import java.util.List;

public class Sala {

    private int id;
    private String codigo;
    private EstadoSala estado;
    private String hostId;

    private List<JugadorEnPartida> jugadores;
    private Partida partidaActual;

    public Sala(int id, String codigo, String hostId) {
        this.id = id;
        this.codigo = codigo;
        this.hostId = hostId;
        this.estado = EstadoSala.ABIERTA;
        this.jugadores = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public EstadoSala getEstado() {
        return estado;
    }

    public String getHostId() {
        return hostId;
    }

    public List<JugadorEnPartida> getJugadores() {
        return jugadores;
    }

    public Partida getPartidaActual() {
        return partidaActual;
    }

    public void agregarJugador(JugadorEnPartida jugador) {
        this.jugadores.add(jugador);
    }

    public boolean puedeIniciar() {
        return !jugadores.isEmpty();
    }

    public void iniciarPartida(Partida partida) {
        if (!puedeIniciar()) {
            throw new IllegalStateException("No se puede iniciar la sala sin jugadores");
        }
        this.partidaActual = partida;
        this.estado = EstadoSala.JUGANDO;
    }
}
