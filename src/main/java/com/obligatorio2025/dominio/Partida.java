package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoPartida;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Partida {

    private int id;
    private EstadoPartida estado;
    private Date inicio;
    private Date fin;
    private ConfiguracionPartida configuracion;
    private List<Ronda> rondas = new ArrayList<>();
    private String jugadorQueDisparoTuttiFrutti;

    public Partida() {
        this.estado = EstadoPartida.CREADA;
    }

    public Partida(int id, ConfiguracionPartida configuracion) {
        this.id = id;
        this.configuracion = configuracion;
        this.estado = EstadoPartida.CREADA;
        this.rondas = new ArrayList<>();
    }

    public void iniciar() {
        this.estado = EstadoPartida.EN_CURSO;
        this.inicio = new Date();
    }

    public void finalizarPorTiempo() {
        this.estado = EstadoPartida.FINALIZADA;
        this.fin = new Date();
    }

    public void finalizarPorTuttiFrutti(String disparadorJugadorId) {
        this.jugadorQueDisparoTuttiFrutti = disparadorJugadorId;

        // si no hay config, cerramos directo
        if (configuracion == null) {
            this.estado = EstadoPartida.FINALIZADA;
            this.fin = new Date();
            return;
        }

        // si la gracia no está habilitada, cerramos directo
        if (!configuracion.isGraciaHabilitar()) {
            this.estado = EstadoPartida.FINALIZADA;
            this.fin = new Date();
            return;
        }

        // si sí hay gracia, pasamos a GRACIA
        this.estado = EstadoPartida.GRACIA;
    }

    public void finalizarDesdeGracia() {
        this.estado = EstadoPartida.FINALIZADA;
        this.fin = new Date();
    }

    public void agregarRonda(Ronda ronda) {
        if (rondas == null) {
            rondas = new ArrayList<>();
        }
        rondas.add(ronda);
    }

    // -------- helpers opcionales --------

    // esto le sirve al ServicioPartida o al Lobby
    public boolean estaActiva() {
        return this.estado == EstadoPartida.EN_CURSO || this.estado == EstadoPartida.GRACIA;
    }

    // -------- getters / setters --------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    public Date getInicio() {
        return inicio;
    }

    public Date getFin() {
        return fin;
    }

    public ConfiguracionPartida getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(ConfiguracionPartida configuracion) {
        this.configuracion = configuracion;
    }

    public List<Ronda> getRondas() {
        return rondas;
    }

    public String getJugadorQueDisparoTuttiFrutti() {
        return jugadorQueDisparoTuttiFrutti;
    }

    // si quisieras permitir cambiarlo desde afuera:
    public void setJugadorQueDisparoTuttiFrutti(String jugadorQueDisparoTuttiFrutti) {
        this.jugadorQueDisparoTuttiFrutti = jugadorQueDisparoTuttiFrutti;
    }
}
