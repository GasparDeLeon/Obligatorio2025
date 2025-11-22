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

    // NUEVO: ranking con posición, manejando empates (solo jugadores que tengan resultados)
    public List<EntradaRankingConPosicion> armarRankingConPosiciones(List<Resultado> resultados) {
        Map<Integer, Integer> puntos = calcularPuntosPorJugador(resultados);

        // armamos una lista ordenada por puntos desc
        List<Map.Entry<Integer, Integer>> ordenados = new ArrayList<>(puntos.entrySet());
        ordenados.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        List<EntradaRankingConPosicion> salida = new ArrayList<>();
        int posicionActual = 0;
        Integer ultimoPuntaje = null;
        int indice = 0;

        for (Map.Entry<Integer, Integer> e : ordenados) {
            indice++;

            if (ultimoPuntaje == null || !ultimoPuntaje.equals(e.getValue())) {
                // nuevo puntaje → nueva posición
                posicionActual = indice;
                ultimoPuntaje = e.getValue();
            }

            salida.add(new EntradaRankingConPosicion(
                    posicionActual,
                    e.getKey(),
                    e.getValue()
            ));
        }

        return salida;
    }

    // NUEVO: ranking con posición, incluyendo jugadores sin resultados (0 puntos)
    public List<EntradaRankingConPosicion> armarRankingConPosicionesIncluyendoJugadores(
            List<Resultado> resultados,
            List<Integer> idsJugadores) {

        Map<Integer, Integer> puntos = new HashMap<>();

        // 1) sumar puntos de resultados
        if (resultados != null) {
            for (Resultado r : resultados) {
                puntos.merge(r.getJugadorId(), r.getPuntos(), Integer::sum);
            }
        }

        // 2) asegurar que todos los jugadores aparezcan aunque tengan 0
        if (idsJugadores != null) {
            for (Integer id : idsJugadores) {
                puntos.putIfAbsent(id, 0);
            }
        }

        // 3) ordenar de mayor a menor puntaje
        List<Map.Entry<Integer, Integer>> lista = new ArrayList<>(puntos.entrySet());
        lista.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // 4) asignar posiciones con empates
        List<EntradaRankingConPosicion> salida = new ArrayList<>();
        int posicionActual = 0;
        int ultimoPuntaje = Integer.MIN_VALUE;
        int indice = 0;

        for (Map.Entry<Integer, Integer> e : lista) {
            indice++;
            int puntaje = e.getValue();

            if (puntaje != ultimoPuntaje) {
                posicionActual = indice;
                ultimoPuntaje = puntaje;
            }

            salida.add(new EntradaRankingConPosicion(
                    posicionActual,
                    e.getKey(),
                    puntaje
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

    // clase para mostrar posición
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
