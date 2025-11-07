package com.obligatorio2025.Controllers;

public class ConfigurarPartidaForm {

    private Integer cantidadRondas;
    private Integer duracionTurnoSeg;
    private Integer tiempoGraciaSeg;
    private String codigoSala;

    // getters y setters
    public Integer getCantidadRondas() { return cantidadRondas; }
    public void setCantidadRondas(Integer cantidadRondas) { this.cantidadRondas = cantidadRondas; }

    public Integer getDuracionTurnoSeg() { return duracionTurnoSeg; }
    public void setDuracionTurnoSeg(Integer duracionTurnoSeg) { this.duracionTurnoSeg = duracionTurnoSeg; }

    public Integer getTiempoGraciaSeg() { return tiempoGraciaSeg; }
    public void setTiempoGraciaSeg(Integer tiempoGraciaSeg) { this.tiempoGraciaSeg = tiempoGraciaSeg; }

    public String getCodigoSala() { return codigoSala; }
    public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }
}
