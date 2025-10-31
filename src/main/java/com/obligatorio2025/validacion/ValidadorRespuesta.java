package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.util.List;

public class ValidadorRespuesta {

    private final CategoriaRepositorio categoriaRepositorio;

    public ValidadorRespuesta(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    public Resultado validar(Partida partida, Respuesta respuesta) {

        // 1. vacío
        if (respuesta.getTexto() == null || respuesta.getTexto().isBlank()) {
            return new Resultado(
                    respuesta.getTexto(),
                    respuesta.getJugadorId(),
                    respuesta.getCategoriaId(),
                    Veredicto.VACIA,
                    "Respuesta vacía",
                    0
            );
        }

        // 2. letra de la ronda
        Ronda rondaActual = obtenerRondaActual(partida);
        if (rondaActual != null) {
            char letra = Character.toUpperCase(rondaActual.getLetra());
            char primera = Character.toUpperCase(respuesta.getTexto().charAt(0));
            if (primera != letra) {
                return new Resultado(
                        respuesta.getTexto(),
                        respuesta.getJugadorId(),
                        respuesta.getCategoriaId(),
                        Veredicto.INVALIDA,
                        "No coincide con la letra de la ronda",
                        0
                );
            }
        }

        // 3. categoría: la palabra debe existir en la lista de esa categoría
        List<String> permitidas = categoriaRepositorio.obtenerPalabrasDe(respuesta.getCategoriaId());
        boolean existe = permitidas
                .stream()
                .anyMatch(p -> p.equalsIgnoreCase(respuesta.getTexto()));
        if (!existe) {
            return new Resultado(
                    respuesta.getTexto(),
                    respuesta.getJugadorId(),
                    respuesta.getCategoriaId(),
                    Veredicto.INVALIDA,
                    "No pertenece a la categoría",
                    0
            );
        }

        // 4. si pasó todo
        return new Resultado(
                respuesta.getTexto(),
                respuesta.getJugadorId(),
                respuesta.getCategoriaId(),
                Veredicto.VALIDA,
                "OK",
                10
        );
    }

    private Ronda obtenerRondaActual(Partida partida) {
        List<Ronda> rondas = partida.getRondas();
        if (rondas == null || rondas.isEmpty()) {
            return null;
        }
        return rondas.get(rondas.size() - 1);
    }
}
