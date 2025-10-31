package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.text.Normalizer;
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
            char letraRonda = Character.toUpperCase(rondaActual.getLetra());

            // normalizar el primer carácter de la respuesta
            String texto = respuesta.getTexto();
            char primeraNormalizada = normalizarPrimerCaracter(texto);

            if (primeraNormalizada != letraRonda) {
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


        // 3. categoría
        List<String> permitidas = categoriaRepositorio.obtenerPalabrasDe(respuesta.getCategoriaId());
        if (permitidas == null || permitidas.isEmpty()) {
            return new Resultado(
                    respuesta.getTexto(),
                    respuesta.getJugadorId(),
                    respuesta.getCategoriaId(),
                    Veredicto.INVALIDA,
                    "Categoría desconocida",
                    0
            );
        }

        String respNorm = normalizar(respuesta.getTexto());

        boolean existe = permitidas
                .stream()
                .map(this::normalizar)
                .anyMatch(p -> p.equals(respNorm));

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

    private String normalizar(String texto) {
        if (texto == null) return "";
        String nfd = Normalizer.normalize(texto, Normalizer.Form.NFD);
        // quita tildes
        String sinTildes = nfd.replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }
    private char normalizarPrimerCaracter(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        // tomamos el primer char tal cual
        char c = texto.charAt(0);

        // lo pasamos a string para normalizar
        String s = String.valueOf(c);

        String nfd = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        // quitamos marcas diacríticas (tildes)
        String sinTilde = nfd.replaceAll("\\p{M}", "");

        return Character.toUpperCase(sinTilde.charAt(0));
    }

}
