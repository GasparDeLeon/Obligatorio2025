package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoSala;
import com.obligatorio2025.dominio.enums.ModoJuego;

import java.util.ArrayList;
import java.util.List;

public class Sala {

    private int id;
    private String codigo;
    private EstadoSala estado;
    private String hostId;
    private List<JugadorEnPartida> jugadores = new ArrayList<>();
    private Partida partidaActual;

    public Sala(int id, String codigo, String hostId) {
        this.id = id; // si id es String en tu modelo
        this.codigo = codigo;
        this.hostId = hostId;
        this.estado = EstadoSala.ABIERTA;
        this.jugadores = new ArrayList<>();
    }

    public boolean puedeIniciar() {
        // 1. estado válido
        if (this.estado != EstadoSala.ABIERTA && this.estado != EstadoSala.PREPARADA) {
            return false;
        }

        // 2. partida + config
        if (this.partidaActual == null || this.partidaActual.getConfiguracion() == null) {
            return false;
        }

        ModoJuego modo = this.partidaActual.getConfiguracion().getModo();
        int cantidad = (jugadores == null) ? 0 : jugadores.size();

        // 3. según modo
        if (modo == ModoJuego.SINGLE) {
            return cantidad >= 1;
        } else {
            return cantidad >= 2 && cantidad <= 6;
        }
    }

    public void agregarJugador(JugadorEnPartida jugador) {
        if (jugadores == null) {
            jugadores = new ArrayList<>();
        }
        jugadores.add(jugador);
    }
    public List<JugadorEnPartida> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<JugadorEnPartida> jugadores) {
        this.jugadores = jugadores;
    }


    public void iniciarPartida() {
        if (!puedeIniciar()) {
            throw new IllegalStateException("La sala no está en condiciones de iniciar.");
        }
        this.estado = EstadoSala.JUGANDO;
    }

    // getters/setters que necesites

    public Partida getPartidaActual() {
        return partidaActual;
    }

    public void setPartidaActual(Partida partidaActual) {
        this.partidaActual = partidaActual;
    }
    public String getCodigo() {
        return codigo;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
