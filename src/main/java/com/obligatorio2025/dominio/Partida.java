package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoPartida;

import java.util.ArrayList;
import java.util.List;

public class Partida {

    private String id;
    private EstadoPartida estado;
    private Sala sala;
    private List<Ronda> rondas;

    public Partida(String id, Sala sala) {
        this.id = id;
        this.sala = sala;
        this.estado = EstadoPartida.CREADA;
        this.rondas = new ArrayList<>();
    }

    public void iniciar() {
        this.estado = EstadoPartida.EN_CURSO;
    }

    public void agregarRonda(Ronda ronda) {
        this.rondas.add(ronda);
    }

    public String getId() {
        return id;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public Sala getSala() {
        return sala;
    }
}
