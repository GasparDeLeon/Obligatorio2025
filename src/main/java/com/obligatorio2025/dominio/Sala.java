package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoSala;

import java.util.ArrayList;
import java.util.List;

public class Sala {

    private String id;
    private String origen;
    private String host;
    private EstadoSala estado;
    private List<Partida> partidas;
    private List<JugadorEnPartida> jugadores;

    public Sala(String id, String origen, String host) {
        this.id = id;
        this.origen = origen;
        this.host = host;
        this.estado = EstadoSala.CREADA;
        this.partidas = new ArrayList<>();
        this.jugadores = new ArrayList<>();
    }

    public boolean puedeIniciar() {
        return this.jugadores.size() >= 2;
    }

    public void agregarJugador(JugadorEnPartida jugador) {
        this.jugadores.add(jugador);
    }

    public void iniciarPartida(Partida partida) {
        this.partidas.add(partida);
        this.estado = EstadoSala.EN_CURSO;
    }
}
