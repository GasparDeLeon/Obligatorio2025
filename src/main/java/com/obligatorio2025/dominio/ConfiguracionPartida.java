package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.dominio.enums.ModoJuez;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfiguracionPartida {

    private int duracionSeg;
    private int duracionGraciaSeg;
    private int rondasTotales;
    private int pausaEntreRondasSeg;
    private ModoJuego modo;
    private boolean graciaHabilitar;
    private ModoJuez modoJuez = ModoJuez.NORMAL;

    // nuevos
    private int puntajeValida;
    private int puntajeDuplicada;

    // NUEVO: máximo de jugadores para esta partida (solo multi)
    // null = usar valor por defecto (6)
    private Integer maxJugadores;

    // NUEVO: categorías elegidas para esta partida (por id)
    private List<Integer> categoriasSeleccionadas = new ArrayList<>();

    public ConfiguracionPartida(int duracionSeg,
                                int duracionGraciaSeg,
                                int rondasTotales,
                                int pausaEntreRondasSeg,
                                ModoJuego modo,
                                boolean graciaHabilitar,
                                int puntajeValida,
                                int puntajeDuplicada) {
        this.duracionSeg = duracionSeg;
        this.duracionGraciaSeg = duracionGraciaSeg;
        this.rondasTotales = rondasTotales;
        this.pausaEntreRondasSeg = pausaEntreRondasSeg;
        this.modo = modo;
        this.graciaHabilitar = graciaHabilitar;
        this.puntajeValida = puntajeValida;
        this.puntajeDuplicada = puntajeDuplicada;
        // maxJugadores queda null por defecto → 6 efectivo
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

    public int getPuntajeValida() {
        return puntajeValida;
    }

    public void setPuntajeValida(int puntajeValida) {
        this.puntajeValida = puntajeValida;
    }

    public int getPuntajeDuplicada() {
        return puntajeDuplicada;
    }

    public void setPuntajeDuplicada(int puntajeDuplicada) {
        this.puntajeDuplicada = puntajeDuplicada;
    }

    // ================= CATEGORÍAS SELECCIONADAS =================

    public List<Integer> getCategoriasSeleccionadas() {
        return Collections.unmodifiableList(categoriasSeleccionadas);
    }

    public void setCategoriasSeleccionadas(List<Integer> categoriasSeleccionadas) {
        if (categoriasSeleccionadas == null) {
            this.categoriasSeleccionadas = new ArrayList<>();
        } else {
            this.categoriasSeleccionadas = new ArrayList<>(categoriasSeleccionadas);
        }
    }

    public void agregarCategoriaSeleccionada(int categoriaId) {
        if (this.categoriasSeleccionadas == null) {
            this.categoriasSeleccionadas = new ArrayList<>();
        }
        if (!this.categoriasSeleccionadas.contains(categoriaId)) {
            this.categoriasSeleccionadas.add(categoriaId);
        }
    }

    public boolean tieneCategoriasConfiguradas() {
        return categoriasSeleccionadas != null && !categoriasSeleccionadas.isEmpty();
    }

    public ModoJuez getModoJuez() {
        return modoJuez;
    }

    public void setModoJuez(ModoJuez modoJuez) {
        this.modoJuez = (modoJuez != null) ? modoJuez : ModoJuez.NORMAL;
    }

    // ================= JUGADORES =================

    public Integer getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(Integer maxJugadores) {
        this.maxJugadores = maxJugadores;
    }

    // Regla general: mínimo 2, máximo 6
    public int getMaxJugadoresEfectivo() {
        int valor = (maxJugadores == null ? 6 : maxJugadores);
        if (valor < 2) valor = 2;
        if (valor > 6) valor = 6;
        return valor;
    }
}
