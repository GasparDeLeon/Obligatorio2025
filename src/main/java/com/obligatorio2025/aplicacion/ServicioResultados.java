package com.obligatorio2025.aplicacion;

import com.obligatorio2025.validacion.Resultado;

import java.util.*;
import java.util.stream.Collectors;

public class ServicioResultados {

    // 1. suma puntos por jugador a partir de la lista de Resultados
    public Map<Integer, Integer> calcularPuntosPorJugador(List<Resultado> resultados) {
        Map<Integer, Integer> puntos = new HashMap<>();
        for (Resultado r : resultados) {
            int jugadorId = r.getJugadorId();
            int actual = puntos.getOrDefault(jugadorId, 0);
            puntos.put(jugadorId, actual + r.getPuntos());
        }
        return puntos;
    }

    // 2. arma el ranking ordenado
    public List<EntradaRanking> armarRanking(List<Resultado> resultados) {
        Map<Integer, Integer> puntosPorJugador = calcularPuntosPorJugador(resultados);

        return puntosPorJugador.entrySet()
                .stream()
                .map(e -> new EntradaRanking(e.getKey(), e.getValue()))
                .sorted((a, b) -> {
                    // primero por puntos descendente
                    int cmp = Integer.compare(b.puntos, a.puntos);
                    if (cmp != 0) return cmp;
                    // empate: por jugadorId ascendente
                    return Integer.compare(a.jugadorId, b.jugadorId);
                })
                .collect(Collectors.toList());
    }

    // 3. versión compatible con tu main actual
    // si querés seguir usando Map y var ranking = servRes.ranking(puntos);
    // podés dejar este método también
    public List<Map.Entry<Integer, Integer>> ranking(Map<Integer, Integer> puntosPorJugador) {
        return puntosPorJugador.entrySet()
                .stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return Integer.compare(a.getKey(), b.getKey());
                })
                .collect(Collectors.toList());
    }

    // DTO simple para la vista / API
    public static class EntradaRanking {
        private final int jugadorId;
        private final int puntos;

        public EntradaRanking(int jugadorId, int puntos) {
            this.jugadorId = jugadorId;
            this.puntos = puntos;
        }

        public int getJugadorId() {
            return jugadorId;
        }

        public int getPuntos() {
            return puntos;
        }

        @Override
        public String toString() {
            return "Jugador " + jugadorId + " -> " + puntos + " pts";
        }
    }
}
