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
    private Marcador marcador;
    private List<Ronda> rondas;

    public Partida(int id, ConfiguracionPartida configuracion) {
        this.id = id;
        this.configuracion = configuracion;
        this.estado = EstadoPartida.CREADA;
        this.rondas = new ArrayList<>();
        this.marcador = new Marcador();
    }

    public int getId() {
        return id;
    }

    public EstadoPartida getEstado() {
        return estado;
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

    public Marcador getMarcador() {
        return marcador;
    }

    public List<Ronda> getRondas() {
        return rondas;
    }

    public void iniciar() {
        this.estado = EstadoPartida.EN_CURSO;
        this.inicio = new Date();
    }

    public void finalizarPorTiempo() {
        this.estado = EstadoPartida.FINALIZADA;
        this.fin = new Date();
    }

    public void finalizarPorTuttiFrutti(String jugadorDisparador) {
        // por ahora solo cambiamos de estado
        this.estado = EstadoPartida.GRACIA;
    }

    public void agregarRonda(Ronda ronda) {
        this.rondas.add(ronda);
    }
}
