package com.obligatorio2025.aplicacion;

import com.obligatorio2025.validacion.Resultado;

import java.util.*;
import java.util.stream.Collectors;

public class ServicioResultados {

    // calcula puntos por jugador a partir de una lista de resultados
    public Map<Integer, Integer> calcularPuntosPorJugador(List<Resultado> resultados) {
        Map<Integer, Integer> puntos = new HashMap<>();
        for (Resultado r : resultados) {
            puntos.merge(r.getJugadorId(), r.getPuntos(), Integer::sum);
        }
        return puntos;
    }

    // el que ya veníamos usando: devuelve lista ordenada de mayor a menor
    public List<EntradaRanking> armarRanking(List<Resultado> resultados) {
        Map<Integer, Integer> puntos = calcularPuntosPorJugador(resultados);
        return puntos.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(e -> new EntradaRanking(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // NUEVO: ranking con posición, manejando empates
    public List<EntradaRankingConPosicion> armarRankingConPosiciones(List<Resultado> resultados) {
        // primero usamos el ranking normal
        List<EntradaRanking> base = armarRanking(resultados);

        List<EntradaRankingConPosicion> salida = new ArrayList<>();
        int posicionActual = 0;
        int ultimoPuntaje = -1;
        int indice = 0;

        for (EntradaRanking entrada : base) {
            indice++;

            if (entrada.getPuntos() != ultimoPuntaje) {
                // nuevo puntaje → nueva posición
                posicionActual = indice;
                ultimoPuntaje = entrada.getPuntos();
            }

            salida.add(new EntradaRankingConPosicion(
                    posicionActual,
                    entrada.getJugadorId(),
                    entrada.getPuntos()
            ));
        }

        return salida;
    }

    // clase de siempre
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

    // nueva clase para mostrar posición
    public static class EntradaRankingConPosicion {
        private final int posicion;
        private final int jugadorId;
        private final int puntos;

        public EntradaRankingConPosicion(int posicion, int jugadorId, int puntos) {
            this.posicion = posicion;
            this.jugadorId = jugadorId;
            this.puntos = puntos;
        }

        public int getPosicion() {
            return posicion;
        }

        public int getJugadorId() {
            return jugadorId;
        }

        public int getPuntos() {
            return puntos;
        }

        @Override
        public String toString() {
            return posicion + "° - Jugador " + jugadorId + " -> " + puntos + " pts";
        }
    }
}
