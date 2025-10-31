package com.obligatorio2025.aplicacion;

import com.obligatorio2025.validacion.Resultado;

import java.util.*;

public class ServicioResultados {

    public Map<Integer, Integer> calcularPuntosPorJugador(List<Resultado> resultados) {
        Map<Integer, Integer> puntos = new HashMap<>();
        for (Resultado r : resultados) {
            int actual = puntos.getOrDefault(r.getJugadorId(), 0);
            puntos.put(r.getJugadorId(), actual + r.getPuntos());
        }
        return puntos;
    }

    public List<Map.Entry<Integer, Integer>> ranking(Map<Integer, Integer> puntos) {
        List<Map.Entry<Integer, Integer>> lista = new ArrayList<>(puntos.entrySet());
        lista.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return lista;
    }
}
