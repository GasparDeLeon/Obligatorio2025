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
        this.puntajeValida = puntajeValida;         // ← esto faltaba
        this.puntajeDuplicada = puntajeDuplicada;   // ← y esto también
        // categoriasSeleccionadas ya se inicializa vacía arriba
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

    /**
     * Devuelve una vista inmodificable para no exponer la lista interna.
     */
    public List<Integer> getCategoriasSeleccionadas() {
        return Collections.unmodifiableList(categoriasSeleccionadas);
    }

    /**
     * Reemplaza la lista de categorías seleccionadas.
     */
    public void setCategoriasSeleccionadas(List<Integer> categoriasSeleccionadas) {
        if (categoriasSeleccionadas == null) {
            throw new IllegalArgumentException("La lista de categorías no puede ser null.");
        }

        if (categoriasSeleccionadas.size() < 4) {
            throw new IllegalArgumentException("Debe seleccionar al menos 4 categorías.");
        }

        if (categoriasSeleccionadas.size() > 8) {
            throw new IllegalArgumentException("No puede seleccionar más de 8 categorías.");
        }

        this.categoriasSeleccionadas = new ArrayList<>(categoriasSeleccionadas);
    }

    /**
     * Agrega una categoría si no estaba ya incluida.
     */
    public void agregarCategoriaSeleccionada(int categoriaId) {
        if (this.categoriasSeleccionadas == null) {
            this.categoriasSeleccionadas = new ArrayList<>();
        }

        if (this.categoriasSeleccionadas.size() >= 8) {
            throw new IllegalStateException("No puede agregar más de 8 categorías.");
        }

        if (!this.categoriasSeleccionadas.contains(categoriaId)) {
            this.categoriasSeleccionadas.add(categoriaId);
        }
    }


    /**
     * Indica si la config tiene por lo menos una categoría elegida.
     */
    public boolean tieneCategoriasConfiguradas() {
        return categoriasSeleccionadas != null && !categoriasSeleccionadas.isEmpty();
    }
    public ModoJuez getModoJuez() {
        return modoJuez;
    }

    public void setModoJuez(ModoJuez modoJuez) {
        this.modoJuez = (modoJuez != null) ? modoJuez : ModoJuez.NORMAL;
    }

}
