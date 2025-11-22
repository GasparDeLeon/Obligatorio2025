package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.enums.ModoJuez;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ConfigurarPartidaForm {

    @NotNull(message = "La cantidad de rondas es obligatoria.")
    @Min(value = 1, message = "Debe haber al menos 1 ronda.")
    @Max(value = 26, message = "No puede haber más de 26 rondas.")
    private Integer cantidadRondas;

    @NotNull(message = "La duración del turno es obligatoria.")
    @Min(value = 10, message = "La duración mínima del turno es de 10 segundos.")
    @Max(value = 300, message = "La duración máxima del turno es de 300 segundos.")
    private Integer duracionTurnoSeg;

    // En modo solo lo vamos a ignorar. Lo usaremos después para multi.
    private Integer tiempoGraciaSeg;

    private String codigoSala;
    private Integer numeroJugadores;
    @NotNull(message = "Debe elegir un modo de juez.")
    private ModoJuez modoJuez;

    public ModoJuez getModoJuez() {
        return modoJuez;
    }

    public void setModoJuez(ModoJuez modoJuez) {
        this.modoJuez = modoJuez;
    }

    // IDs de categorías elegidas
    private List<Integer> categoriasSeleccionadas = new ArrayList<>();

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

    public List<Integer> getCategoriasSeleccionadas() {
        return categoriasSeleccionadas;
    }

    public void setCategoriasSeleccionadas(List<Integer> categoriasSeleccionadas) {
        this.categoriasSeleccionadas = categoriasSeleccionadas;
    }
}
