package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.dominio.Ronda;

import java.util.List;

public class ValidadorRespuesta {

    public Resultado validar(Partida partida, Respuesta respuesta) {

        // 1. sin texto
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

        // 2. obtener la ronda actual (la última)
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

        // 3. si pasó todo:
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
