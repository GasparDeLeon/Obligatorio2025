package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.ModoJuego;

public class ConfiguracionPartida {

    private int duracionSeg;
    private int duracionGraciaSeg;
    private int rondasTotales;
    private int pausaEntreRondasSeg;
    private ModoJuego modo;
    private boolean graciaHabilitar;

    public ConfiguracionPartida(int duracionSeg,
                                int duracionGraciaSeg,
                                int rondasTotales,
                                int pausaEntreRondasSeg,
                                ModoJuego modo,
                                boolean graciaHabilitar) {
        this.duracionSeg = duracionSeg;
        this.duracionGraciaSeg = duracionGraciaSeg;
        this.rondasTotales = rondasTotales;
        this.pausaEntreRondasSeg = pausaEntreRondasSeg;
        this.modo = modo;
        this.graciaHabilitar = graciaHabilitar;
    }

    public int getDuracionSeg() {
        return duracionSeg;
    }

    public int getDuracionGraciaSeg() {
        return duracionGraciaSeg;
    }

    public int getRondasTotales() {
        return rondasTotales;
    }

    public int getPausaEntreRondasSeg() {
        return pausaEntreRondasSeg;
    }

    public ModoJuego getModo() {
        return modo;
    }

    public boolean isGraciaHabilitar() {
        return graciaHabilitar;
    }
}
