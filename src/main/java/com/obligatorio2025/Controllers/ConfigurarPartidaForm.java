package com.obligatorio2025.Controllers;

import java.util.ArrayList;
import java.util.List;

public class ConfigurarPartidaForm {

    private Integer cantidadRondas;
    private Integer duracionTurnoSeg;
    private Integer tiempoGraciaSeg;
    private String codigoSala;
    private Integer numeroJugadores;
    private List<Integer> categoriasSeleccionadas = new ArrayList<>();

    // getters/setters existentes...

    public Integer getNumeroJugadores() {
        return numeroJugadores;
    }

    public void setNumeroJugadores(Integer numeroJugadores) {
        this.numeroJugadores = numeroJugadores;
    }

    public Integer getCantidadRondas() {
        return cantidadRondas;
    }

    public void setCantidadRondas(Integer cantidadRondas) {
        this.cantidadRondas = cantidadRondas;
    }

    public Integer getDuracionTurnoSeg() {
        return duracionTurnoSeg;
    }

    public void setDuracionTurnoSeg(Integer duracionTurnoSeg) {
        this.duracionTurnoSeg = duracionTurnoSeg;
    }

    public Integer getTiempoGraciaSeg() {
        return tiempoGraciaSeg;
    }

    public void setTiempoGraciaSeg(Integer tiempoGraciaSeg) {
        this.tiempoGraciaSeg = tiempoGraciaSeg;
    }

    public String getCodigoSala() {
        return codigoSala;
    }

    public void setCodigoSala(String codigoSala) {
        this.codigoSala = codigoSala;
    }

    // getters/setters nuevos

    public List<Integer> getCategoriasSeleccionadas() {
        return categoriasSeleccionadas;
    }

    public void setCategoriasSeleccionadas(List<Integer> categoriasSeleccionadas) {
        this.categoriasSeleccionadas = categoriasSeleccionadas;
    }
}
