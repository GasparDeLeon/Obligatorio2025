package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.text.Normalizer;
import java.util.List;

public class ValidadorRespuesta {

    private final CategoriaRepositorio categoriaRepositorio;
    private final ServicioIA servicioIA;

    public ValidadorRespuesta(CategoriaRepositorio categoriaRepositorio,
                              ServicioIA servicioIA) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.servicioIA = servicioIA;
    }

    public Resultado validar(Partida partida, Respuesta respuesta) {

        String textoOriginal = respuesta.getTexto();

        // 1. vacío
        if (textoOriginal == null || textoOriginal.isBlank()) {
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
        char letraRonda = 0;
        if (rondaActual != null) {
            letraRonda = Character.toUpperCase(rondaActual.getLetra());

            char primeraNormalizada = normalizarPrimerCaracter(textoOriginal);

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

        // 3. llamar a la IA para saber si la palabra pertenece a la categoría
        ServicioIA.VeredictoIA verIA = servicioIA.validar(
                respuesta.getCategoriaId(),
                letraRonda,
                textoOriginal
        );

        if (!verIA.isValida()) {
            return new Resultado(
                    respuesta.getTexto(),
                    respuesta.getJugadorId(),
                    respuesta.getCategoriaId(),
                    Veredicto.INVALIDA,
                    verIA.getMotivo(),
                    0
            );
        }

        // 4. si pasó todo → VALIDA (puntos luego puede ajustarlos JuezBasico)
        return new Resultado(
                respuesta.getTexto(),
                respuesta.getJugadorId(),
                respuesta.getCategoriaId(),
                Veredicto.VALIDA,
                verIA.getMotivo(),
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
        String sinTildes = nfd.replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }

    private char normalizarPrimerCaracter(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        char c = texto.charAt(0);
        String s = String.valueOf(c);
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String sinTilde = nfd.replaceAll("\\p{M}", "");
        return Character.toUpperCase(sinTilde.charAt(0));
    }
}
