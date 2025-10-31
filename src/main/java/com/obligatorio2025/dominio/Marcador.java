package com.obligatorio2025.dominio;

import java.util.HashMap;
import java.util.Map;

public class Marcador {

    private Map<Integer, Integer> puntajesPorJugador;

    public Marcador() {
        this.puntajesPorJugador = new HashMap<>();
    }

    public int puntajeDe(int jugadorId) {
        return puntajesPorJugador.getOrDefault(jugadorId, 0);
    }

    public void setPuntaje(int jugadorId, int pts) {
        puntajesPorJugador.put(jugadorId, pts);
    }

    public void sumarPuntaje(int jugadorId, int pts) {
        int actual = puntajeDe(jugadorId);
        puntajesPorJugador.put(jugadorId, actual + pts);
    }
}
